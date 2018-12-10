package com.jad.r4j.boiler.impl.sensor;

import com.pi4j.component.temperature.TemperatureSensorBase;

/**
 * @since 12/08/2018
 */
public abstract class AbstractTemprSensor extends TemperatureSensorBase {

    public double getTemperatureRounded() {
       return ((double)Math.round(getTemperature() * 100)) / 100;
    }
}
