package com.jad.r4j.boiler.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class SystemConfigTest {

    private static final String EXAMPLE_KEY = "test.key";
    private static final String TEST_DB_FILE = "test.db";
    private Injector configInjector;
    private LocalStorageConfig localStorageConfig;
    private Configuration config;

    @Before
    public void setUp() throws Exception {
        tearDown();
        init();
    }

    private void init() throws Exception {
        configInjector = Guice.createInjector(new SystemConfig());
        config = configInjector.getInstance(Configuration.class);
        localStorageConfig = new LocalStorageConfig(TEST_DB_FILE, config);
    }

    @Test
    public void getSetting() throws Exception {
        int origin = config.getInt(EXAMPLE_KEY);
        int newVal = origin + 1;
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        config.registerListener("test", stringTuple -> {
            if (stringTuple.getA().equals("test.key")
                    && String.valueOf(stringTuple.getB()).equals(String.valueOf(newVal))) {
                atomicBoolean.set(true);
            }
        });
        config.update(EXAMPLE_KEY, Integer.class, newVal);
        Assert.assertTrue("Listener not working", atomicBoolean.get());
        localStorageConfig.destroy();
        init();
        Assert.assertEquals(newVal, config.getInt(EXAMPLE_KEY));
    }

    @After
    public void tearDown() {
        File file = new File(TEST_DB_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}