package com.jad.r4j.boiler.impl;

import com.google.inject.Singleton;
import com.jad.r4j.boiler.config.Schedule;
import com.jad.r4j.boiler.impl.sensor.AbstractTemprSensor;
import com.jad.r4j.boiler.impl.sensor.MCP3208TemperatureSensor;
import com.pi4j.component.relay.Relay;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;

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
public class SensorsProvider {
    private final Relay boilerRelay;
    private final AbstractTemprSensor boilerOutputTemp;
    private final AbstractTemprSensor kitchen;

    @Inject
    public SensorsProvider(@Named("boilerMain") Relay boilerRelay,
                           @Named("boilerOutputTemp") AbstractTemprSensor boilerOutputTemp,
                           @Named("kitchenTemp") AbstractTemprSensor kitchen) {
        this.boilerRelay = boilerRelay;
        this.boilerOutputTemp = boilerOutputTemp;
        this.kitchen = kitchen;
    }

    public double getCurrentRoomTemperature() {
        return kitchen.getTemperatureRounded();
    }

    public double getCurrentBoilerTemperature() {
        return boilerOutputTemp.getTemperatureRounded();
    }



    public boolean isBoilerOff() {
        return boilerRelay.isOpen();
    }


    public void boilerTurnOn() {
        //todo logging
        boilerRelay.close();
    }

    public void boilerTurnOff() {
        boilerRelay.open();
    }

    @Schedule(10000)
    public void printBoilerOutTemp() {
        log.info("Boiler out temp {}", getCurrentBoilerTemperature());
    }
}
