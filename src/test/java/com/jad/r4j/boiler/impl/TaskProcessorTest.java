package com.jad.r4j.boiler.impl;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
 * @since 11/03/2018
 */
public class TaskProcessorTest {

    @Test
    public void testProcessTasks() throws Exception {
        final TaskProcessor taskProcessor = new TaskProcessor();
        final List<Integer> integers = new ArrayList<>();
        taskProcessor.schedule(() -> integers.add(1), 0, TimeUnit.MILLISECONDS);
        taskProcessor.schedule(() -> integers.add(2), 10, TimeUnit.MILLISECONDS);
        taskProcessor.schedule(() -> Thread.currentThread().interrupt(), 10, TimeUnit.MILLISECONDS);
        taskProcessor.processTasks();
        assertEquals(Arrays.asList(1,2), integers);

    }
}