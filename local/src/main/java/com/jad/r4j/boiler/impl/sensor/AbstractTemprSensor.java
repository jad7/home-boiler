package com.jad.r4j.boiler.impl.sensor;

import com.pi4j.component.temperature.TemperatureSensorBase;

public abstract class AbstractTemprSensor extends TemperatureSensorBase {
   public AbstractTemprSensor() {
   }

   public double getTemperatureRounded() {
      return (double)Math.round(this.getTemperature() * 100.0D) / 100.0D;
   }
}
