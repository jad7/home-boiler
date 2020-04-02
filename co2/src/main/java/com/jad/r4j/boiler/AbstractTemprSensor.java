package com.jad.r4j.boiler;

import com.jad.r4j.boiler.utils.Functions;
import com.pi4j.component.temperature.TemperatureSensorBase;

public abstract class AbstractTemprSensor extends TemperatureSensorBase {
   public AbstractTemprSensor() {
   }

   public double getTemperatureRounded() {
      return Functions.round(this.getTemperature());
   }
}
