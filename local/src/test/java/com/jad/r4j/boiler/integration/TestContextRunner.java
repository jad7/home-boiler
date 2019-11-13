package com.jad.r4j.boiler.integration;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.jad.r4j.boiler.config.SystemConfig;
import com.jad.r4j.boiler.impl.TaskProcessor;
import com.jad.r4j.boiler.v2.controller.ModuleV2;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TestContextRunner {

    public Injector testContext() {
        Injector configInj = Guice.createInjector(new SystemConfig());
        Injector childInjector = configInj.createChildInjector(Modules.override(new ModuleV2()).with(new TestModule()));
        return childInjector;
    }

    @Test
    public void runContext() throws InterruptedException {
        TaskProcessor instance = testContext().getInstance(TaskProcessor.class);
        instance.schedule(() -> {throw new TestException();}, 10, TimeUnit.SECONDS);
        try {
            instance.processTasks();
            Assert.fail();
        } catch (TestException e) {

        }
    }

    private static class TestException extends RuntimeException {

    }
}
