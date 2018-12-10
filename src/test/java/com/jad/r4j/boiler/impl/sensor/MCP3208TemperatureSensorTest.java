package com.jad.r4j.boiler.impl.sensor;

import com.jad.r4j.boiler.config.Configuration;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2018 Art.com, All Rights Reserved
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
 * @since 11/05/2018
 */
public class MCP3208TemperatureSensorTest {

    @Test
    public void testConvertToTemp() throws Exception {
        Configuration.debug = true;
        System.out.println(Math.log1p(Math.E * Math.E - 1));
        final MCP3208TemperatureSensor mcp3208TemperatureSensor = new MCP3208TemperatureSensor(null,
                3950, 25d, 10_000d, 3.3, 10_000, 4096, false, "bla");
        final MCP3208TemperatureSensor.Point v = mcp3208TemperatureSensor.convertToTemp(2220);
        System.out.println(v);
    }

}