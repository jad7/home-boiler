package com.jad.r4j.boiler.impl;

import com.jad.r4j.boiler.config.Schedule;
import com.jad.r4j.boiler.dto.DisplayError;
import com.jad.r4j.boiler.impl.sensor.TM1637Python;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class DisplayController {
   private SensorsProvider sensorsProvider;
   private TM1637Python displayManager;

   @Inject
   public DisplayController(SensorsProvider sensorsProvider, @Named("tm1637Dispay") TM1637Python displayManager) {
      this.sensorsProvider = sensorsProvider;
      this.displayManager = displayManager;
   }

   @Schedule(
      value = 10,
      timeUnit = TimeUnit.SECONDS,
      startImmediately = false
   )
   public void updateDisplay() {
      this.displayManager.setDigit(this.sensorsProvider.getCurrentRoomTemperature());
   }

   public void showLisa() {
      this.displayManager.setLisa();
   }

   public void showError(DisplayError error) {
      this.displayManager.setError(error.getCode());
   }

   public void showChanged() {
      this.displayManager.changed();
   }
}
