package com.jad.r4j.boiler;

import com.pi4j.temperature.TemperatureScale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class MCP3208TemperatureSensor extends AbstractTemprSensor {
   private static final Logger log = LoggerFactory.getLogger(MCP3208TemperatureSensor.class);
   private final Supplier<Integer> analogInput;
   private final Integer termistrorB;
   private final Double bTemperature;
   private final Double bResistance;
   private final Double voltage;
   private final Integer resistorR1;
   private final Integer adcMaxValue;
   private final String name;
   private final MCP3208TemperatureSensor.Point[] valuesCache;
   private final RingBuffer<Double> value = new RingBuffer(5);

   public MCP3208TemperatureSensor(Supplier<Integer> analogInput, Integer termistrorB, Double bTemperature, Double bResistance, Double voltage, Integer resistorR1, Integer adcMaxValue, boolean cacheValues, String name) {
      this.analogInput = analogInput;
      this.termistrorB = termistrorB;
      this.bTemperature = bTemperature;
      this.bResistance = bResistance;
      this.voltage = voltage;
      this.resistorR1 = resistorR1;
      this.adcMaxValue = adcMaxValue;
      this.name = name;
      if (cacheValues) {
         this.valuesCache = new MCP3208TemperatureSensor.Point[adcMaxValue];
      } else {
         this.valuesCache = null;
      }
      this.init();
   }

   private void init() {
      if (this.valuesCache != null) {
         for(int i = 0; i < this.adcMaxValue; ++i) {
            MCP3208TemperatureSensor.Point point = this.convertToTemp(i);
            this.valuesCache[i] = point;
         }
      }

   }

   public double getTemperature() {
      return this.value.mean();
   }

   private double getTemperature0() {
      Integer value = this.analogInput.get();
      if (this.valuesCache != null) {
         MCP3208TemperatureSensor.Point point = this.valuesCache[value];
         return point.temperature;
      } else {
         return this.convertToTemp(value).temperature;
      }
   }

   protected MCP3208TemperatureSensor.Point convertToTemp(int value) {
      double V = (double)value * this.voltage / (double)this.adcMaxValue;
      double R = (double)this.resistorR1 / (this.voltage / V - 1.0D);
      double T = 1.0D / (Math.log1p(R / this.bResistance - 1.0D) / (double)this.termistrorB + 1.0D / (273.15D + this.bTemperature)) - 273.15D;

      return new MCP3208TemperatureSensor.Point(value, V, R, T);
   }

   public TemperatureScale getScale() {
      return TemperatureScale.CELSIUS;
   }

   public void schedule() throws Exception {
      double temperature0 = this.getTemperature0();
      this.value.add(temperature0);
   }

   @Override
   public String getName() {
      return name;
   }

   protected class Point {
      private final int adcValue;
      private final double voltage;
      private final double resistance;
      private final double temperature;

      public String toString() {
         return "MCP3208TemperatureSensor.Point(adcValue=" + this.adcValue + ", voltage=" + this.voltage + ", resistance=" + this.resistance + ", temperature=" + this.temperature + ")";
      }

      public Point(int adcValue, double voltage, double resistance, double temperature) {
         this.adcValue = adcValue;
         this.voltage = voltage;
         this.resistance = resistance;
         this.temperature = temperature;
      }
   }
}
