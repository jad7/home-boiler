package com.jad.r4j.boiler;

import lombok.Value;

@Value
public class SensorValue {
    String name;
    Double value;
    long time = System.currentTimeMillis();
}
