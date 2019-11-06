package com.jad.r4j.boiler.impl.sensor;

import com.pi4j.temperature.TemperatureScale;
import java.util.logging.Logger;

public class StubTemperatureSensor extends AbstractTemprSensor {
   private static final Logger log = Logger.getLogger(StubTemperatureSensor.class.getName());

   public StubTemperatureSensor() {
   }

   public double getTemperature() {
      log.warning("Triggered stub temperature sensor");
      return 0.0D;
   }

   public TemperatureScale getScale() {
      return TemperatureScale.CELSIUS;
   }
}
