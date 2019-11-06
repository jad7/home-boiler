package com.jad.r4j.boiler.impl;

import com.google.inject.Singleton;
import com.jad.r4j.boiler.config.Configuration;
import com.jad.r4j.boiler.config.Initiable;
import com.jad.r4j.boiler.dto.BoilerSchedule;
import lombok.Data;

import java.beans.ConstructorProperties;
import java.util.UUID;
import javax.inject.Inject;

@Singleton
public class DynamicConfigurationHolder implements Initiable {
   private final Configuration configuration;
   private DynamicConfigurationHolder.MinMax atHome = new DynamicConfigurationHolder.MinMax();
   private DynamicConfigurationHolder.MinMax notAtHome = new DynamicConfigurationHolder.MinMax();
   private boolean anyBodyAtHomeAutomatic = false;
   private DynamicConfigurationHolder.Manual<Boolean> anyBodyAtHomeManual;
   private Integer manualDurationMinutes;
   private BoilerSchedule boilerSchedule;

   @Inject
   public DynamicConfigurationHolder(Configuration configuration) {
      this.configuration = configuration;
   }

   public void init() throws Exception {
      Configuration prefix = this.configuration.getConfigByPrefix("config.temperature.");
      this.notAtHome.min = prefix.getDouble("min");
      this.notAtHome.max = prefix.getDouble("max");
      this.atHome.min = prefix.getDouble("atHome.min");
      this.atHome.max = prefix.getDouble("atHome.max");
      this.manualDurationMinutes = prefix.getInt("manual.duration.minutes");
      this.boilerSchedule = BoilerSchedule.parse(this.configuration.getStr("config.periods.atHome.working"), this.configuration.getStr("config.periods.atHome.weekend"));
   }

   public boolean isManual() {
      return this.anyBodyAtHomeManual != null;
   }

   public void setAnyBodyAtHomeManual(boolean value, UUID uuid) {
      this.anyBodyAtHomeManual = new DynamicConfigurationHolder.Manual(value, uuid);
   }

   public void clearManual() {
      this.anyBodyAtHomeManual = null;
   }

   public boolean isAnyBodyAtHome() {
      return this.anyBodyAtHomeManual != null ? (Boolean)this.anyBodyAtHomeManual.t : this.anyBodyAtHomeAutomatic;
   }

   public double minWhenNotAtHomeTemperature() {
      return this.notAtHome.min;
   }

   public double minWhenAtHomeTemperature() {
      return this.atHome.min;
   }

   public double maxWhenAtHomeTemperature() {
      return this.atHome.max;
   }

   public double maxWhenNotAtHomeTemperature() {
      return this.notAtHome.max;
   }

   public boolean isAnyBodyAtHomeAutomatic() {
      return this.anyBodyAtHomeAutomatic;
   }

   public void setAnyBodyAtHomeAutomatic(boolean anyBodyAtHomeAutomatic) {
      this.anyBodyAtHomeAutomatic = anyBodyAtHomeAutomatic;
   }

   public DynamicConfigurationHolder.Manual<Boolean> getAnyBodyAtHomeManual() {
      return this.anyBodyAtHomeManual;
   }

   public Integer getManualDurationMinutes() {
      return this.manualDurationMinutes;
   }

   public BoilerSchedule getBoilerSchedule() {
      return this.boilerSchedule;
   }

   static class MinMax {
      double min;
      double max;

      MinMax() {
      }
   }

   @Data
   public static class Manual<T> {
      private T t;
      private UUID uuid;

      @ConstructorProperties({"t", "uuid"})
      public Manual(T t, UUID uuid) {
         this.t = t;
         this.uuid = uuid;
      }


   }
}
