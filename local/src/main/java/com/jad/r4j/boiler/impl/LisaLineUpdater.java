package com.jad.r4j.boiler.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jad.r4j.boiler.config.Schedule;
import java.util.concurrent.TimeUnit;

@Singleton
public class LisaLineUpdater {
   private DisplayController controller;

   @Inject
   public LisaLineUpdater(DisplayController controller) {
      this.controller = controller;
   }

   @Schedule(
      value = 5,
      timeUnit = TimeUnit.MINUTES,
      startImmediately = false
   )
   public void showLisa() {
      this.controller.showLisa();
   }
}
