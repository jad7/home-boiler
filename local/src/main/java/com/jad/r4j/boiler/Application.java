package com.jad.r4j.boiler;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jad.r4j.boiler.config.RaspberryConfig;
import com.jad.r4j.boiler.config.SystemConfig;
import com.jad.r4j.boiler.impl.TaskProcessor;

public class Application {
   public Application() {
   }

   public static void main(String[] args) throws InterruptedException {
      Injector configInjector = Guice.createInjector(new SystemConfig());
      Injector injector = configInjector.createChildInjector(new RaspberryConfig(configInjector));
      TaskProcessor taskProcessor = injector.getInstance(TaskProcessor.class);
      taskProcessor.processTasks();
   }

}
