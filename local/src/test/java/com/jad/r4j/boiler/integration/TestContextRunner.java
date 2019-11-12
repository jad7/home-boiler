package com.jad.r4j.boiler.integration;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.jad.r4j.boiler.config.SystemConfig;
import com.jad.r4j.boiler.impl.TaskProcessor;
import com.jad.r4j.boiler.v2.controller.ModuleV2;
import org.junit.Test;

public class TestContextRunner {

    public Injector testContext() throws InterruptedException {
        Injector configInj = Guice.createInjector(new SystemConfig());
        Injector childInjector = configInj.createChildInjector(Modules.override(new ModuleV2()).with(new TestModule()));
        return childInjector;
    }

    @Test
    public void runContext() throws InterruptedException {
        testContext().getInstance(TaskProcessor.class).processTasks();
    }
}
