package com.jad.r4j.boiler.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

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
 * @since 11/04/2018
 */
public class ConfigurationTest {

    @Test
    public void testGetByPrefix() throws Exception {
        final Map<String, String> map = new HashMap<>();
        map.put("a.b.c.d", "1");
        map.put("a.b.e", "2");
        map.put("a.c.e.b", "3");
        map.put("a.b.g", "4");
        map.put("c.d.a.b", "5");

        final Configuration configuration = new Configuration(map);

        final Map<String, String> byPrefix = configuration.getByPrefix("a.b.", true);
        Assert.assertEquals(new HashSet(Arrays.asList("1", "2", "4")),
                new HashSet(byPrefix.values()));

    }
}