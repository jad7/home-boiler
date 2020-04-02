package com.jad.r4j.boiler;

import com.pi4j.gpio.extension.mcp.MCP3208Pin;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiMode;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {
    private MHZ19 mhz19;
    private MCP3208TemperatureSensor temprSensor;
    private InfluxDBDao influxDBDao;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final Scheduler scheduler = Schedulers.from(executor);

    public static void main(String[] args) {
        Main main = new Main();
        try {
            main.init();
        } catch (Exception e) {
            log.error("Catch error on init", e);
            main.close();
            return;
        }
        main.run();
    }

    private void init() throws IOException {
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
    }

    private void run() {
        Observable.fromArray(mhz19.getObservable(scheduler), createTemperatureObservable())
                .flatMap(t -> t, false, 2, 1)
                .doOnNext(influxDBDao::consume)
                .subscribeOn(scheduler)
                .doOnTerminate(this::close)
                .blockingSubscribe();

    }

    private void close() {
        if (mhz19 != null) {
            mhz19.close();
        }
        if (influxDBDao != null) {
            influxDBDao.close();
        }
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
