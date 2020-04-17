package com.jad.r4j.boiler;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

@Value
@AllArgsConstructor
public class SensorValue {
    String name;
    Double value;
    long time;

    public SensorValue(String name, Double value) {
        this.name = name;
        this.value = value;
        time = System.currentTimeMillis();
    }

    public String asJson() {
        return "{" +
                    //"\"name\":\"" + name +"\"," +
                    "\"value\":" + value + "," +
                    "\"time\":\"" + Instant.ofEpochMilli(time).toString() + "\"" +
                "}";
    }
}
