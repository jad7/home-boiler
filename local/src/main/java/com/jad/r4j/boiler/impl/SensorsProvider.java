package com.jad.r4j.boiler.impl;

import com.google.inject.Singleton;
import com.jad.r4j.boiler.config.Schedule;
import com.jad.r4j.boiler.impl.sensor.AbstractTemprSensor;
import com.pi4j.component.relay.Relay;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SensorsProvider {
   private static final Logger log = LoggerFactory.getLogger(SensorsProvider.class);
   private final Relay boilerRelay;
   private final AbstractTemprSensor boilerOutputTemp;
   private final AbstractTemprSensor kitchen;

   @Inject
   public SensorsProvider(@Named("boilerMain") Relay boilerRelay, @Named("boilerOutputTemp") AbstractTemprSensor boilerOutputTemp, @Named("kitchenTemp") AbstractTemprSensor kitchen) {
      this.boilerRelay = boilerRelay;
      this.boilerOutputTemp = boilerOutputTemp;
      this.kitchen = kitchen;
   }

   public double getCurrentRoomTemperature() {
      return this.kitchen.getTemperatureRounded();
   }

   public double getCurrentBoilerTemperature() {
      return this.boilerOutputTemp.getTemperatureRounded();
   }

   public boolean isBoilerOff() {
      return this.boilerRelay.isOpen();
   }

   public void boilerTurnOn() {
      this.boilerRelay.close();
   }

   public void boilerTurnOff() {
      this.boilerRelay.open();
   }

   @Schedule(10000)
   public void printBoilerOutTemp() {
      log.info((String)"Boiler out temp {}", (Object)this.getCurrentBoilerTemperature());
   }
}
