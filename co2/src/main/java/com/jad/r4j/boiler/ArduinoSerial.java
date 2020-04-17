package com.jad.r4j.boiler;


import com.pi4j.io.serial.*;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@Slf4j
public class ArduinoSerial implements AutoCloseable {
    private final Serial serial = SerialFactory.createInstance();
    private Path path = Paths.get("/dev/ttyUSB1");
    private BufferedReader reader;
    private volatile boolean inited = false;


    public ArduinoSerial() {
    }

    private void init() {
        close();
        path = null;
        path = initPath();
        if (path != null && Files.exists(path)) {
            SerialConfig serialConfig = new SerialConfig();
            serialConfig.baud(Baud._9600);
            serialConfig.parity(Parity.NONE);
            serialConfig.dataBits(DataBits._8);
            serialConfig.stopBits(StopBits._1);
            serialConfig.device(path.toString());

            try {
                this.serial.open(serialConfig);
            } catch (IOException var3) {
                throw new RuntimeException(var3);
            }
            reader = new BufferedReader(new InputStreamReader(serial.getInputStream()));
            inited = true;
        }
    }

    private Path initPath() {
        try {
            List<Path> ttyUSBs = Files.list(Paths.get("/dev"))
                    .filter(dirPath -> dirPath.toFile().getName().startsWith("ttyUSB"))
                    .collect(Collectors.toList());
            if (!ttyUSBs.isEmpty()) {
                path = ttyUSBs.get((int) (Math.random() * ttyUSBs.size()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    //PM2.5: 123, Humidity: 42.60, Temp: 21.80, CO2: 678

    public List<SensorValue> read() throws IOException {
        if (!inited) {
            init();
        }
        if (inited && Files.exists(path)) {
            try {
                return readData();
            } catch (Exception e) {
                //ignore
            }
        } else {
            inited = false;
        }
        return Collections.emptyList();
    }

    private List<SensorValue> readData() throws IOException {
        if (!reader.ready()) {
            return Collections.emptyList();
        }
        String s = "";
        while (reader.ready()) {
            s = reader.readLine();
            //log.debug("Has been read: {}", s);

        }
        StringTokenizer strTkn = new StringTokenizer(s, ",");
        @AllArgsConstructor
        class Tuple<T> {String name; T value;}
        return enumerationAsStream(strTkn)
                .map(String::valueOf)
                .map(str -> {
                    int ind = str.indexOf(":");
                    return new Tuple<>(str.substring(0, ind).trim(), str.substring(ind + 2));
                })
                .map(tpl -> {
                    try {
                        return new Tuple<>(tpl.name, Double.valueOf(tpl.value));
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(tpl -> new SensorValue(tpl.name, tpl.value))
                .collect(Collectors.toList());
    }


    //Test
    public static void main(String[] args) {
        ArduinoSerial arduinoSerial = new ArduinoSerial();
        Observable.interval(5, 25, TimeUnit.SECONDS, Schedulers.computation())
                .map(i -> arduinoSerial.read())
                .retry()
                .doOnNext(System.out::println)
                .blockingSubscribe();
    }

    public static <T> Stream<T> enumerationAsStream(Enumeration<T> e) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new Iterator<T>() {
                            public T next() {
                                return e.nextElement();
                            }
                            public boolean hasNext() {
                                return e.hasMoreElements();
                            }
                        },
                        Spliterator.ORDERED), false);
    }

    @Override
    public void close() {
        if (reader != null) {
            try (BufferedReader br = reader) {
            } catch (IOException e) {
                //ignore
            }
        }
        if (serial.isOpen()) {
            try (Serial s1 = this.serial) {
            } catch (IOException var2) {
                //ignore
            }
        }

    }




    public Observable<SensorValue> getObservable(Scheduler scheduler) {
        return Observable.interval(5, 25, TimeUnit.SECONDS, scheduler)
                .map(i -> read())
                .flatMap(Observable::fromIterable, false, 1, 1)
                .map(sv -> new SensorValue("ard_" + sv.getName(), sv.getValue(), sv.getTime()));
                //.retry(throwable -> !Thread.currentThread().isInterrupted());
    }

    /*@Data
    public static class ArduinoData {
        private Integer co2;
        private Double humidity;
        private Double temp;
        private Integer pm25;
    }*/
}
