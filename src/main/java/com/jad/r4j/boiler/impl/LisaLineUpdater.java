package com.jad.r4j.boiler.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jad.r4j.boiler.config.Schedule;

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
 * @since 11/10/2018
 */
@Singleton
public class LisaLineUpdater {
    private DisplayController controller;

    @Inject
    public LisaLineUpdater(DisplayController controller) {
        this.controller = controller;
    }

    @Schedule(value = 5, timeUnit = TimeUnit.MINUTES, startImmediately = false)
    public void showLisa() {
        controller.showLisa();
    }
}
