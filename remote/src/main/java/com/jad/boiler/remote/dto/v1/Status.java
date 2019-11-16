package com.jad.boiler.remote.dto.v1;

import lombok.Data;

@Data
public class Status {
   private boolean manual;
   private Status.State state;
   private Double tempFrom;
   private Double tempTo;
   private Double currentTemperature;
   private boolean relayEnabled;



   public static enum State {
      AT_HOME,
      NOT_AT_HOME;
   }
}
