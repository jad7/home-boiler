package com.jad.r4j.boiler;

import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class InfluxDBDao {
    final InfluxDB influxDB;

    public InfluxDBDao() {
        final String serverURL = "http://127.0.0.1:8086", username = "root", password = "root";
        influxDB = InfluxDBFactory.connect(serverURL);
        log.info("Connected to DB version: {}", influxDB.version());

        String databaseName = "boiler";
        influxDB.setDatabase(databaseName);
        influxDB.setRetentionPolicy("two_days");
        influxDB.enableBatch();

    }

    public void copy() {
        class Val {int i = 0;};
        final Val val = new Val();
        influxDB.query(new Query("SELECT \"value\" FROM \"sensors_fix\""), 1000, qr -> {
            qr.getResults().forEach(result -> {
                result.getSeries().forEach(series -> {
                    series.getValues().forEach(list -> {
                        val.i++;
                        Object o = list.get(1);
                        if (o instanceof Double) {
                            String tag;
                            Double d = (Double) o;
                            if (d < 25) {
                                tag = "kitchenTemp";
                            } else {
                                tag = "co2";
                            }
                            influxDB.write(Point.measurement("sensors")
                                    .time(Instant.parse(list.get(0).toString()).toEpochMilli(), TimeUnit.MILLISECONDS)
                                    .tag("sensor", tag)
                                    .addField("value", d)
                                    .build());
                        }
                    });
                });
            });
            System.out.println("Done: " + val.i);
        });
    }

    public static void main(String[] args) {
        InfluxDBDao influxDBDao = new InfluxDBDao();
        influxDBDao.copy();
        influxDBDao.close();
    }

    public void consume(SensorValue sensorValue) {
        try {
            influxDB.write(Point.measurement("sensors")
                    .time(sensorValue.getTime(), TimeUnit.MILLISECONDS)
                    .tag("sensor", sensorValue.getName())
                    .addField("value", sensorValue.getValue())
                    .build());
        } catch (Exception e) {
            log.error("InfluxDB can not consume value", e);
        }
    }

    public void close() {
        if (influxDB != null) {
            influxDB.flush();
            influxDB.close();
        }
    }
}
