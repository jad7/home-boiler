package com.jad.r4j.boiler;

import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.util.concurrent.TimeUnit;

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

    public void consume(SensorValue sensorValue) {
        influxDB.write(Point.measurement("sensors")
                .time(sensorValue.getTime(), TimeUnit.MILLISECONDS)
                .tag("sensor ", sensorValue.getName())
                .addField("value", sensorValue.getValue())
                .build());
    }

    public void close() {
        if (influxDB != null) {
            influxDB.flush();
            influxDB.close();
        }
    }
}
