package com.jad.boiler.remote.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Action {
   private String name;
   private String value;
   private LocalDateTime applied;
}
