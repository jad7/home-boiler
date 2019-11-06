package com.jad.r4j.boiler.impl.controller;

import com.jad.r4j.boiler.config.Schedule;
import com.jad.r4j.boiler.utils.RingBuffer;
import lombok.Data;

import java.beans.ConstructorProperties;
import javax.inject.Provider;

public class TemperatureMonitor {
   private final Provider<Integer> sensor;
   private final RingBuffer<TemperatureMonitor.MonitorPoint> buffer;

   public TemperatureMonitor(Provider<Integer> sensor) {
      this.sensor = sensor;
      this.buffer = new RingBuffer(7200);
   }

   @Schedule(500)
   public void getTemp() {
      this.buffer.add(new TemperatureMonitor.MonitorPoint((double)(Integer)this.sensor.get(), System.currentTimeMillis()));
   }

   @Data
   private static class MonitorPoint {
      private double value;
      private long time;



      @ConstructorProperties({"value", "time"})
      public MonitorPoint(double value, long time) {
         this.value = value;
         this.time = time;
      }
   }
}
