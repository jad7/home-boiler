package com.jad.r4j.boiler;

import lombok.Value;

import java.time.Instant;

@Value
public class SensorValue {
    String name;
    Double value;
    long time = System.currentTimeMillis();

    public String asJson() {
        return "{" +
                    //"\"name\":\"" + name +"\"," +
                    "\"value\":" + value + "," +
                    "\"time\":\"" + Instant.ofEpochMilli(time).toString() + "\"" +
                "}";
    }
}
