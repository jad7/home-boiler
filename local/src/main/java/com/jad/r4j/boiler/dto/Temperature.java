package com.jad.r4j.boiler.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Temperature {
   private TemperatureSensorLocation location;
   private double value;
   private LocalDateTime dateTime;

}
