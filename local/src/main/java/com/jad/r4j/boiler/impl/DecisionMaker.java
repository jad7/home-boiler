package com.jad.r4j.boiler.impl;

import com.jad.r4j.boiler.config.ConfigurationParent;
import com.jad.r4j.boiler.config.Initiable;
import com.jad.r4j.boiler.config.Schedule;
import com.jad.r4j.boiler.dto.BoilerSchedule;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DecisionMaker implements Initiable {
   private static final Logger log = LoggerFactory.getLogger(DecisionMaker.class);
   private SensorsProvider sensorsProvider;
   private Provider<DynamicConfigurationHolder> config;
   private TaskProcessor taskProcessor;
   private AtomicReference<DateProcessor> dateProcessorRef = new AtomicReference<>();

   @Inject
   public DecisionMaker(SensorsProvider sensorsProvider, Provider<DynamicConfigurationHolder> config, TaskProcessor taskProcessor) {
      this.sensorsProvider = sensorsProvider;
      this.config = config;
      this.taskProcessor = taskProcessor;
   }

   public void init() {
      BoilerSchedule boilerSchedule = (this.config.get()).getBoilerSchedule();
      this.dateProcessorRef.set(new DateProcessor(boilerSchedule));
      this.taskProcessor.scheduleRepeatable(() -> {
         DateProcessor dateProcessor = this.dateProcessorRef.get();
         boolean atHome = dateProcessor.isInRangeNow();
         (this.config.get()).setAnyBodyAtHomeAutomatic(atHome);
      }, 1L, TimeUnit.MINUTES);
   }

   @Schedule(10000)
   public void doDecision() {
      double currentTemp = this.sensorsProvider.getCurrentRoomTemperature();
      DynamicConfigurationHolder holder = this.config.get();
      boolean anyAtHome = holder.isAnyBodyAtHome();
      if (ConfigurationParent.debug) {
         log.info("Home temperature {} at home: {}", currentTemp, anyAtHome);
      }

      if (this.sensorsProvider.isBoilerOff()) {
         if (currentTemp <= holder.minWhenNotAtHomeTemperature() || anyAtHome && currentTemp <= holder.minWhenAtHomeTemperature()) {
            log.info("Home temperature {} at home: {} boiler switched to ON", currentTemp, anyAtHome);
            this.sensorsProvider.boilerTurnOn();
         }
      } else if (anyAtHome && currentTemp >= holder.maxWhenAtHomeTemperature() || !anyAtHome && currentTemp >= holder.maxWhenNotAtHomeTemperature()) {
         log.info("Home temperature {} at home: {} boiler switched to OFF", currentTemp, anyAtHome);
         this.sensorsProvider.boilerTurnOff();
      }

   }
}
