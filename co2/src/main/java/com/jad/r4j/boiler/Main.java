package com.jad.r4j.boiler;

import com.pi4j.gpio.extension.mcp.MCP3208Pin;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiMode;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class Main {
    public static final URL VIBER_BOT_STATUS;

    static {
        try {
            VIBER_BOT_STATUS = URI.create("https://totoro-viber-bot.herokuapp.com/status").toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private final ArduinoSerial arduinoSerial = new ArduinoSerial();
    private MHZ19 mhz19;
    private MCP3208TemperatureSensor temprSensor;
    private InfluxDBDao influxDBDao;
    private ScheduledExecutorService executor;
    private Scheduler scheduler;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient okHttpClient;

    /*private final HttpClient httpClient = HttpClient.newBuilder()
            .executor(executor)
            .connectTimeout(Duration.ofSeconds(10))
            .build();*/


    public static void main(String[] args) throws InterruptedException {
        Main main = new Main();
        boolean successInited = false;
        do {
            try {
                main.init();
                successInited = true;
            } catch (Exception e) {
                log.error("Catch error on init", e);
                main.close();
                TimeUnit.SECONDS.sleep(10);
            }
        } while (!Thread.currentThread().isInterrupted() && !successInited);
        main.run();
    }

    private void init() throws IOException {
        executor = Executors.newScheduledThreadPool(1);
        scheduler = Schedulers.from(executor);
        mhz19 = new MHZ19();
        log.info("CO2 sensor ready");
        SpiChannel spiCh = SpiChannel.getByNumber(0);
        MCP3208Controller mcp3208Controller = new MCP3208Controller(spiCh, 1000000, SpiMode.MODE_0, 3.3d);
        log.info("MCP3208 configured on SPI: {}", spiCh);
        temprSensor = new MCP3208TemperatureSensor(() -> mcp3208Controller.pinReaderSignal(MCP3208Pin.CH0),
                3380, 25d, 10000d,
                3.3d, 10000, mcp3208Controller.getMaxValue(),
                true, "kitchenTemp"
            );
        log.info("Temperature sensor ready");
        influxDBDao = new InfluxDBDao();
        log.info("DB connection ready");
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(1, 6, TimeUnit.HOURS))
                .readTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .writeTimeout(10, TimeUnit.SECONDS)
                .cache(null)
                .build();
        log.info("REST client ready");
    }

    private void run() {
        final Map<String, SensorValue> state = new HashMap<>(2);
        Observable.fromArray(mhz19.getObservable(scheduler),
                    createTemperatureObservable(),
                    arduinoSerial.getObservable(scheduler)
                )
                .retry(throwable -> !Thread.currentThread().isInterrupted())
                .flatMap(t -> t, false, 3, 1)
                .doOnNext(influxDBDao::consume)
                .doOnNext(val -> state.put(val.getName(), val))
                .throttleLatest(1, TimeUnit.MINUTES, scheduler)
                .doOnNext(v -> send(state))
                .retry(throwable -> !Thread.currentThread().isInterrupted())
                .subscribeOn(scheduler)
                .doOnTerminate(this::close)
                .blockingSubscribe();
    }

    private void send(Map<String, SensorValue> state) throws IOException {
        try {
            sendOkHttp(state);
        } catch (Exception e) {
            log.error("Http error", e);
        }
    }
    private void sendOkHttp(Map<String, SensorValue> state) throws IOException {
        Request request = new Request.Builder()
                .url(VIBER_BOT_STATUS)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(JSON, toJson(state)))
                .build();
        okHttpClient.newCall(request).execute();
    }

    private void sendJavaUrl(Map<String, SensorValue> state) throws IOException {
        HttpURLConnection con = (HttpURLConnection) VIBER_BOT_STATUS.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setConnectTimeout(10_000);
        con.setDoOutput(true);
        byte[] data = toJson(state).getBytes();
        con.setRequestProperty("Content-Length", Integer.toString(data.length));
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.write(data);
        wr.close();

        InputStream is = con.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();
        con.disconnect();
        /*HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(VIBER_BOT_STATUS)
                .POST(HttpRequest.BodyPublishers.ofString())
                .build();
        httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());*/
    }

    private static String toJson(Map<String, SensorValue> state) {
        return state.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":" + e.getValue().asJson())
                .collect(Collectors.joining(",", "{", "}"));
    }

    private void close() {
        if (mhz19 != null) {
            mhz19.close();
        }
        if (influxDBDao != null) {
            influxDBDao.close();
        }
        if (okHttpClient != null) {
            try {
                okHttpClient.dispatcher().executorService().shutdown();
                okHttpClient.connectionPool().evictAll();
                if (okHttpClient.cache() != null) {
                    okHttpClient.cache().close();
                }
            } catch (Exception e) {
                //ignore
            } finally {
                okHttpClient = null;
            }
        }
        executor.shutdown();
    }

    private Observable<SensorValue> createTemperatureObservable() {
        executor.scheduleAtFixedRate(() -> {
            try {
                temprSensor.schedule();
            } catch (Exception e) {
                log.error("Error on temp observable creation", e);
                throw new RuntimeException(e);
            }
        }, 0, 2, TimeUnit.SECONDS);
        return Observable.interval(10, 10, TimeUnit.SECONDS, scheduler)
                .map(v -> temprSensor.getTemperatureRounded())
                .map(val -> new SensorValue(temprSensor.getName(), val));
    }


}
