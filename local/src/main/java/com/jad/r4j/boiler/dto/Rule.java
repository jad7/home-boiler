package com.jad.r4j.boiler.dto;

public interface Rule {
   int priority();

   boolean isAppliableNow();
}
