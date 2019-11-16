package com.jad.boiler.remote.dto.v2;

import lombok.Data;

@Data
public class Config {
    private String mode;
    private Float minTHome;
    private Float maxTHome;

}
