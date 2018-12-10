package com.jad.r4j.boiler.impl;

import com.jad.r4j.boiler.config.Schedule;
import com.jad.r4j.boiler.dto.DisplayError;
import com.jad.r4j.boiler.impl.sensor.TM1637Python;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

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
 * @since 11/08/2018
 */
@Singleton
public class DisplayController {
    private SensorsProvider sensorsProvider;
    private TM1637Python displayManager;

    @Inject
    public DisplayController(SensorsProvider sensorsProvider, @Named("tm1637Dispay") TM1637Python displayManager) {
        this.sensorsProvider = sensorsProvider;
        this.displayManager = displayManager;
    }

    @Schedule(value = 10, timeUnit = TimeUnit.SECONDS, startImmediately = false)
    public void updateDisplay() {
        displayManager.setDigit(sensorsProvider.getCurrentRoomTemperature());
    }

    public void showLisa() {
        displayManager.setLisa();
    }

    public void showError(DisplayError error) {
        displayManager.setError(error.getCode());
    }

    public void showChanged() {
        displayManager.changed();
    }
}
