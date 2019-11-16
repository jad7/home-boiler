package com.jad.boiler.remote.dto.v2;

import lombok.Data;

@Data
public class Status {
   private String mode;
   private String state;

   private Double tempFrom;
   private Double tempTo;
   private Double currentTemperature;
   private boolean relayEnabled;



}
