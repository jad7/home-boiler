package com.jad.r4j.boiler;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jad.r4j.boiler.config.SystemConfig;
import com.jad.r4j.boiler.impl.TaskProcessor;
import com.jad.r4j.boiler.v2.controller.BoilerController;
import com.jad.r4j.boiler.v2.controller.ModuleV2;

public class Application {
   public Application() {
   }

   public static void main(String[] args) throws InterruptedException {
      Injector configInjector = Guice.createInjector(new SystemConfig());
      Injector injector =
              // configInjector.createChildInjector(new RaspberryConfig(configInjector));
               configInjector.createChildInjector(new ModuleV2());
      TaskProcessor taskProcessor = injector.getInstance(TaskProcessor.class);
      taskProcessor.processTasks();
   }

}
