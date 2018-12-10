package com.jad.r4j.boiler.impl.sensor;

import com.google.common.base.Stopwatch;
import com.jad.r4j.boiler.config.Configuration;
import com.jad.r4j.boiler.config.Schedule;
import com.jad.r4j.boiler.utils.RingBuffer;
import com.pi4j.component.temperature.TemperatureSensorBase;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.temperature.TemperatureScale;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**

 * <p>
 * Developed by Grid Dynamics International, Inc. for the customer Art.com.
 * http://www.griddynamics.com
 * <p>
 * Classification level: Confidential
 * <p>
 * EXCEPT EXPRESSED BY WRITTEN WRITING, THIS CODE AND INFORMATION ARE PROVIDED "AS IS"
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * For information about the licensing and copyright of this document please
 * contact Grid Dynamics at info@griddynamics.com.
 *
 * @since 11/03/2018
 */
@Slf4j
public class MCP3208TemperatureSensor extends AbstractTemprSensor {

    private final Provider<Integer> analogInput;
    private final Integer termistrorB;
    private final Double bTemperature;
    private final Double bResistance;
    private final Double voltage;
    private final Integer resistorR1;
    private final Integer adcMaxValue;
    private final String name;
    private final Point[] valuesCache;
    private final RingBuffer<Double> value = new RingBuffer<>(5);

    public MCP3208TemperatureSensor(Provider<Integer> analogInput,
                                    Integer termistrorB,
                                    Double bTemperature,
                                    Double bResistance,
                                    Double voltage,
                                    Integer resistorR1,
                                    Integer adcMaxValue,
                                    boolean cacheValues,
                                    String name) {
        this.analogInput = analogInput;
        this.termistrorB = termistrorB;
        this.bTemperature = bTemperature;
        this.bResistance = bResistance;
        this.voltage = voltage;
        this.resistorR1 = resistorR1;
        this.adcMaxValue = adcMaxValue;
        this.name = name;
        if (cacheValues) {
            valuesCache = new Point[adcMaxValue];
        } else {
            valuesCache = null;
        }
        final Stopwatch started = Stopwatch.createStarted();
        init();
        log.info("MCP sensor inited for {}ms", started.elapsed(TimeUnit.MILLISECONDS));

    }

    private void init() {
        if (valuesCache != null) {
            for (int i = 0; i < adcMaxValue; i++) {
                final Point point = convertToTemp(i);
                valuesCache[i] = point;
            }
        }
    }

    @Override
    public double getTemperature() {
        return value.mean();
    }



    private double getTemperature0() {
        final Integer value = analogInput.get();
        if (valuesCache != null) {
            final Point point = valuesCache[value];
            return point.temperature;
        } else {
            return convertToTemp(value).temperature;
        }
        //log.info("ADC: {} Temperature: {}", value, point.temperature);
    }

    protected Point convertToTemp(int value) {

        double V = value * voltage / adcMaxValue; //Voltage

        double R = resistorR1/ (voltage / V - 1);

        double T =          1 /
                    (Math.log1p(R / bResistance - 1)
                        / termistrorB + 1 / (273.15 +  bTemperature)) - 273.15;
        if (Configuration.debug) {
            log.info("Sensor: {}, ADC: {}, Voltage: {}, Resistance: {}, Temperature: {}"
                    , new Object[]{name, value, V, R, T});
        }
        return new Point(value, V, R, T);
    }

    @Override
    public TemperatureScale getScale() {
        return TemperatureScale.CELSIUS;
    }

    @Schedule(2000)
    public void schedule() throws Exception {
        final double temperature0 = getTemperature0();
        value.add(temperature0);
    }

    @ToString
    @AllArgsConstructor
    protected class Point {
        private final int adcValue;
        private final double voltage;
        private final double resistance;
        private final double temperature;

    }
}
