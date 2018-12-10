package com.jad.r4j.boiler.impl.controller;

import com.jad.r4j.boiler.config.Schedule;
import com.jad.r4j.boiler.utils.RingBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.inject.Provider;

/**
 * @since 11/27/2018
 */
public class TemperatureMonitor {

    private final Provider<Integer> sensor;

    private final RingBuffer<MonitorPoint> buffer;

    public TemperatureMonitor(Provider<Integer> sensor) {
        this.sensor = sensor;
        this.buffer = new RingBuffer<>(3600 * 2);
    }

    @Schedule(500)
    public void getTemp() {
        buffer.add(new MonitorPoint(sensor.get(), System.currentTimeMillis()));
    }

    @Data
    @AllArgsConstructor
    private static class MonitorPoint {
        private double value;
        private long time;
    }
}
