package com.jad.r4j.boiler.impl;

import com.google.inject.Singleton;
import com.jad.r4j.boiler.config.Configuration;
import com.jad.r4j.boiler.config.Initiable;
import com.jad.r4j.boiler.dto.BoilerSchedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

/**

 * <p>
 * Developed by Grid Dynamics International, Inc. for the customer Art.com.
 * http://www.griddynamics.com
 * <p>
 * Classification level: Confidential
 * <p>
 * EXCEPT EXPRESSED BY WRITTEN WRITING, THIS CODE AND INFORMATION ARE PROVIDED "AS IS"
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * For information about the licensing and copyright of this document please
 * contact Grid Dynamics at info@griddynamics.com.
 *
 * @since 11/03/2018
 */
@Singleton
public class DynamicConfigurationHolder implements Initiable {
    private final Configuration configuration;

    private MinMax atHome = new MinMax();
    private MinMax notAtHome = new MinMax();

    @Getter @Setter
    private boolean anyBodyAtHomeAutomatic = false;
    @Getter
    private Manual<Boolean> anyBodyAtHomeManual;

    @Getter
    private Integer manualDurationMinutes;

    @Getter
    private BoilerSchedule boilerSchedule;

    @AllArgsConstructor
    @Data
    public static class Manual<T> {
        private T t;
        private UUID uuid;
    }

    static class MinMax {
        double min;
        double max;
    }

    @Inject
    public DynamicConfigurationHolder(Configuration configuration) {
        this.configuration = configuration;
    }


    @Override
    public void init() throws Exception {
        Configuration prefix = configuration.getConfigByPrefix("config.temperature.");
        notAtHome.min = prefix.getDouble("min");
        notAtHome.max = prefix.getDouble("max");
        atHome.min = prefix.getDouble("atHome.min");
        atHome.max = prefix.getDouble("atHome.max");
        manualDurationMinutes = prefix.getInt("manual.duration.minutes");
        boilerSchedule = BoilerSchedule.parse(
                configuration.getStr("config.periods.atHome.working"),
                configuration.getStr("config.periods.atHome.weekend"));

    }


    public boolean isManual() {
        return anyBodyAtHomeManual != null;
    }

    public void setAnyBodyAtHomeManual(boolean value, UUID uuid) {
        this.anyBodyAtHomeManual = new Manual<>(value, uuid);
    }

    public void clearManual() {
        anyBodyAtHomeManual = null;
    }

    public boolean isAnyBodyAtHome() {
        return anyBodyAtHomeManual  != null ? anyBodyAtHomeManual.t : anyBodyAtHomeAutomatic;
    }


    public double minWhenNotAtHomeTemperature() {
        return notAtHome.min;
    }

    public double minWhenAtHomeTemperature() {
        return atHome.min;
    }

    public double maxWhenAtHomeTemperature() {
        return atHome.max;
    }

    public double maxWhenNotAtHomeTemperature() {
        return notAtHome.max;
    }
}
