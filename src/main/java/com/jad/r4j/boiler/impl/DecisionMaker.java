package com.jad.r4j.boiler.impl;

import com.jad.r4j.boiler.config.Configuration;
import com.jad.r4j.boiler.config.Initiable;
import com.jad.r4j.boiler.config.Schedule;
import com.jad.r4j.boiler.dto.BoilerSchedule;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
@Slf4j
@Singleton
public class DecisionMaker implements Initiable {
    private SensorsProvider sensorsProvider;
    private Provider<DynamicConfigurationHolder> config;
    private TaskProcessor taskProcessor;

    private AtomicReference<DateProcessor> dateProcessorRef = new AtomicReference<>();

    @Inject
    public DecisionMaker(SensorsProvider sensorsProvider,
                         Provider<DynamicConfigurationHolder> config,
                         TaskProcessor taskProcessor) {
        this.sensorsProvider = sensorsProvider;
        this.config = config;
        this.taskProcessor = taskProcessor;
    }

    @Override
    public void init() throws Exception {
        final BoilerSchedule boilerSchedule = config.get().getBoilerSchedule();
        dateProcessorRef.set(new DateProcessor(boilerSchedule));
        taskProcessor.scheduleRepitedForever(() -> {
            final DateProcessor dateProcessor = dateProcessorRef.get();
            final boolean atHome = dateProcessor.isInRangeNow();
            config.get().setAnyBodyAtHomeAutomatic(atHome);
        }, 1, TimeUnit.MINUTES);
    }

    @Schedule(value = 10_000)
    public void doDecision() {
        double currentTemp = sensorsProvider.getCurrentRoomTemperature();
        final DynamicConfigurationHolder holder = config.get();
        boolean anyAtHome = holder.isAnyBodyAtHome();
        if (Configuration.debug) {
            log.info("Home temperature {} at home: {}", currentTemp, anyAtHome);
        }
        if (sensorsProvider.isBoilerOff()) {
            if (currentTemp <= holder.minWhenNotAtHomeTemperature()
                || (anyAtHome && currentTemp <= holder.minWhenAtHomeTemperature())) {
                log.info("Home temperature {} at home: {} boiler switched to ON", currentTemp, anyAtHome);
                sensorsProvider.boilerTurnOn();
            }
        } else {
            if ((anyAtHome && currentTemp >= holder.maxWhenAtHomeTemperature())
                    || (!anyAtHome && currentTemp >= holder.maxWhenNotAtHomeTemperature())) {
                    log.info("Home temperature {} at home: {} boiler switched to OFF", currentTemp, anyAtHome);
                    sensorsProvider.boilerTurnOff();
            }
        }

        //taskProcessor.schedule(this::doDecision, 30, TimeUnit.SECONDS);
    }

    /*@Override
    public void init() throws Exception {
        taskProcessor.schedule(this::doDecision, 1, TimeUnit.SECONDS);
    }*/
}
