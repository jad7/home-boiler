package com.jad.r4j.boiler.impl.controller;

import com.google.inject.Singleton;
import com.jad.r4j.boiler.config.Schedule;
import com.jad.r4j.boiler.impl.SensorsProvider;
import java.util.concurrent.TimeUnit;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AutomaticTemperatureController {
   private static final Logger log = LoggerFactory.getLogger(AutomaticTemperatureController.class);
   private TemperatureMonitor monitor;
   private AutomaticTemperatureController.Command currentMode;
   private SensorsProvider sensorsProvider;

   public AutomaticTemperatureController() {
   }

   @Schedule(
      value = 30,
      timeUnit = TimeUnit.SECONDS,
      startImmediately = false
   )
   public void process() {
      if (this.currentMode != null) {
         switch(this.currentMode.getMode()) {
         case LIGHT:
         case AUTO:
         default:
            break;
         case AGGRESSIVE:
            double currentRoomTemperature = this.sensorsProvider.getCurrentRoomTemperature();
            if (this.sensorsProvider.isBoilerOff()) {
               if (currentRoomTemperature < (double)this.currentMode.getFrom()) {
                  this.sensorsProvider.boilerTurnOn();
                  log.info((String)"Boiler switched to ON by command {}, temperature", (Object)this.currentMode.getName(), (Object)currentRoomTemperature);
               }
            } else if (currentRoomTemperature >= (double)this.currentMode.getTo()) {
               this.sensorsProvider.boilerTurnOff();
               log.info((String)"Boiler switched to OFF by command {}, temperature", (Object)this.currentMode.getName(), (Object)currentRoomTemperature);
            }
         }
      }

   }

   public static enum Mode {
      LIGHT,
      AGGRESSIVE,
      AUTO;

      private Mode() {
      }
   }

   @Data
   public static class Command {
      private String name;
      private AutomaticTemperatureController.Mode mode;
      private float from;
      private float to;

      public Command() {
         this.mode = AutomaticTemperatureController.Mode.AUTO;
      }

      public String getName() {
         return this.name;
      }

      public AutomaticTemperatureController.Mode getMode() {
         return this.mode;
      }


   }
}
