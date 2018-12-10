package com.jad.r4j.boiler.impl.controller;

import com.google.inject.Singleton;
import com.jad.r4j.boiler.config.Schedule;
import com.jad.r4j.boiler.impl.SensorsProvider;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @since 11/25/2018
 */

@Slf4j
@Singleton
public class AutomaticTemperatureController {

    private TemperatureMonitor monitor;

    private Command currentMode;

    private SensorsProvider sensorsProvider;

    @Schedule(value = 30, timeUnit = TimeUnit.SECONDS, startImmediately = false)
    public void process() {
        if (currentMode != null) {
            switch (currentMode.getMode()) {

                case LIGHT:

                    break;
                case AGGRESSIVE:
                    final double currentRoomTemperature = sensorsProvider.getCurrentRoomTemperature();
                    if (sensorsProvider.isBoilerOff()) {
                        if (currentRoomTemperature < currentMode.getFrom()) {
                            sensorsProvider.boilerTurnOn();
                            log.info("Boiler switched to ON by command {}, temperature", currentMode.getName(), currentRoomTemperature);
                        }
                    } else {
                        if (currentRoomTemperature >= currentMode.getTo()) {
                            sensorsProvider.boilerTurnOff();
                            log.info("Boiler switched to OFF by command {}, temperature", currentMode.getName(), currentRoomTemperature);
                        }
                    }
                    break;
                case AUTO:
                    
                    break;
            }
        }
    }

    @Data
    public static class Command {
        private String name;
        private Mode mode = Mode.AUTO;
        private float from;
        private float to;
    }

    public enum Mode {
        LIGHT, AGGRESSIVE, AUTO
    }


}
