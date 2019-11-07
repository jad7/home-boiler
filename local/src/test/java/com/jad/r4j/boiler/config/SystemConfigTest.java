package com.jad.r4j.boiler.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class SystemConfigTest {

    private static final String EXAMPLE_KEY = "components.relay.boiler.offstate.pin";
    private Injector configInjector;
    private LocalStorageConfig localStorageConfig;
    private Configuration config;

    @Before
    public void setUp() throws Exception {
        configInjector = Guice.createInjector(new SystemConfig());
        config = configInjector.getInstance(Configuration.class);
        localStorageConfig = new LocalStorageConfig("test.db", config);
    }

    @Test
    public void getSetting() throws Exception {
        int origin = config.getInt(EXAMPLE_KEY);
        int newVal = origin + 1;
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        config.registerListener("components.relay.boiler", stringTuple -> {
            if (stringTuple.getA().equals("components.relay.boiler.offstate.pin")
                    && String.valueOf(stringTuple.getB()).equals(String.valueOf(newVal))) {
                atomicBoolean.set(true);
            }
        });
        config.update(EXAMPLE_KEY, Integer.class, newVal);
        Assert.assertTrue("Listener not working", atomicBoolean.get());
        localStorageConfig.destroy();
        setUp();
        Assert.assertEquals(newVal, config.getInt(EXAMPLE_KEY));
    }
}