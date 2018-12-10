package com.jad.r4j.boiler.config;

import com.google.inject.*;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.ProvisionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.jad.r4j.boiler.impl.*;
import com.jad.r4j.boiler.impl.sensor.AbstractTemprSensor;
import com.jad.r4j.boiler.impl.sensor.MCP3208TemperatureSensor;
import com.jad.r4j.boiler.impl.sensor.StubTemperatureSensor;
import com.jad.r4j.boiler.impl.sensor.TM1637Python;
import com.pi4j.component.relay.Relay;
import com.pi4j.component.relay.impl.GpioRelayComponent;
import com.pi4j.gpio.extension.mcp.MCP3208Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiMode;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 10/07/2018
 */

@Slf4j
public class RaspberryConfig extends AbstractModule {

    private final Injector configInjector;
    private final Hook hook = new Hook();

    public RaspberryConfig(Injector configInjector) {
        this.configInjector = configInjector;
    }

    @Singleton
    @Provides
    public GpioController gpioController() {
        return GpioFactory.getInstance();
    }

    @Named("boilerMain")
    @Singleton
    @Provides
    public Relay boilerSwitcherRelay(@Named("components.relay.boiler.main.pin") int pinNum,
                                     GpioController gpioController) {
        final Pin pin = RaspiPin.getPinByAddress(pinNum);

        final GpioPinDigitalOutput digitalOutput = gpioController.provisionDigitalOutputPin(pin);
        return new GpioRelayComponent(digitalOutput);
    }

    @Singleton
    @Provides
    public MCP3208Controller mcp3208(@Named("components.mcp3208.spi") int spi,
                                     @Named("components.mcp3208.spi.speed") int spiSpeed,
                                     @Named("components.mcp3208.voltage") double voltage) throws IOException {
        //final GpioController gpio = gpioController;
        final SpiChannel spiCh = SpiChannel.getByNumber(spi);
        final MCP3208Controller mcp3208Controller = new MCP3208Controller(spiCh, spiSpeed, SpiMode.MODE_0, voltage);
        log.info("MCP3208 configured on SPI: {}", spiCh);
        return mcp3208Controller;
    }

    @Provides
    @Named("analogInput0")
    public Integer mcpCH0(MCP3208Controller controller) {
        return controller.pinReaderSignal(MCP3208Pin.CH0);
    }

    /*static int ch = 0;
    static Pin[] pins = new Pin[] {MCP3208Pin.CH0,
            MCP3208Pin.CH1,
            MCP3208Pin.CH2,
            MCP3208Pin.CH3,
            MCP3208Pin.CH4,
            MCP3208Pin.CH5,
            MCP3208Pin.CH6,
            MCP3208Pin.CH7,

    };*/
    @Provides
    @Named("analogInput1")
    public Integer mcpCH1(MCP3208Controller controller) {
        /*final int i = ch++ & 7;
        final Pin pin = pins[i];
        log.info("Get ch: {}", pin);*/
        return controller.pinReaderSignal(MCP3208Pin.CH7);
    }


    @Singleton
    @Provides
    @Named("kitchenTemp")
    public AbstractTemprSensor kitchenSensor(@Named("analogInput0") Provider<Integer> input,
                                                  MCP3208Controller controller,
                                                  Configuration globalConfig) {
        if (globalConfig.getBool("components.mcp3208.ch0.enabled")) {
            final Configuration config = globalConfig.getConfigByPrefix("components.mcp3208.ch0.params.");
            return new MCP3208TemperatureSensor(input,
                    config.getInt("b"),
                    config.getDouble("bTemperature"),
                    config.getDouble("bResistance"),
                    controller.getVoltage(),
                    config.getInt("shoulderResistor"),
                    controller.getMaxValue(),
                    config.getBool("cacheValues"),
                    "kitchenSensor");
        } else {
            return new StubTemperatureSensor();
        }
    }

    @Singleton
    @Provides
    @Named("boilerOutputTemp")
    public AbstractTemprSensor boilerSensor(@Named("analogInput1") Provider<Integer> input,
                                                  MCP3208Controller controller,
                                                  Configuration globalConfig) {
        if (globalConfig.getBool("components.mcp3208.ch1.enabled")) {
            final Configuration config = globalConfig.getConfigByPrefix("components.mcp3208.ch1.params.");
            return new MCP3208TemperatureSensor(input,
                    config.getInt("b"),
                    config.getDouble("bTemperature"),
                    config.getDouble("bResistance"),
                    controller.getVoltage(),
                    config.getInt("shoulderResistor"),
                    controller.getMaxValue(), config.getBool("cacheValues"),
                    "boilerSensor");
        } else {
            return new StubTemperatureSensor();
        }
    }

    @Singleton
    @Provides
    @Named("tm1637Dispay")
    public TM1637Python dispay(@Named("config.display.script.path") String path) {
        return new TM1637Python(new File(path));
    }


    @Override
    protected void configure() {
        Runtime.getRuntime().addShutdownHook(hook);
        bindUpdatableSchedule();
        bind(DynamicConfigurationHolder.class);
        bind(DecisionMaker.class).asEagerSingleton();
        bind(DisplayController.class).asEagerSingleton();
        bind(LisaLineUpdater.class).asEagerSingleton();
        bind(RemoteServiceUpdater.class).asEagerSingleton();

    }

    private void bindUpdatableSchedule() {
        final Provider<TaskProcessor> taskProcessorProvider = getProvider(TaskProcessor.class);

        final Map<Class, Anns> cache = new HashMap<>();

        final Matcher<Class> classMatcher = Matchers.inSubpackage("com.jad.r4j.boiler");
        bindListener(Matchers.any(), new ProvisionListener() {
            @Override
            public <T> void onProvision(ProvisionInvocation<T> provision) {
                final T obj = provision.provision();
                //log.info("Created: {} by key {}", obj.getClass().getCanonicalName(), provision.getBinding().getKey());
                if (obj instanceof Initiable) {
                    try {
                        ((Initiable) obj).init();
                    } catch (Exception e) {
                        throw new RuntimeException("Can not init object: " + provision.getBinding().getKey() , e);
                    }
                }
                if (obj instanceof Destroyable) {
                    hook.destroyList.add(((Destroyable) obj)::destroy);
                }
                final Anns anns = cache.computeIfAbsent(obj.getClass(), typeLiteral -> {
                    final Class rawType = typeLiteral;
                    if (!classMatcher.matches(rawType)) {
                        return new Anns(null, null, null);
                    }
                    for (Method method : rawType.getDeclaredMethods()) {
                        final Updatable updateble = method.getDeclaredAnnotation(Updatable.class);
                        if (updateble != null) {
                            log.info("Found @Updatable for class: {}", rawType.getName());
                            return new Anns(updateble, null, method);
                        }
                        final Schedule schedule = method.getDeclaredAnnotation(Schedule.class);
                        if (schedule != null) {
                            log.info("Found @Schedule for class: {}", rawType.getName());
                            return new Anns(null, schedule, method);
                        }
                    }
                    return new Anns(null, null, null);
                });
                processSchedul(obj, anns, taskProcessorProvider);
            }
        });

    }

    @AllArgsConstructor private static class  Anns {
        Updatable updatable;
        Schedule schedule;
        Method method;
    }

    private <I> void processSchedul(I i, Anns anns, Provider<TaskProcessor> taskProcessorProvider) {
        if (anns != null && anns.method != null) {
            if (anns.updatable != null) {
                taskProcessorProvider.get().schedule(() -> {
                    try {
                        anns.method.invoke(i);
                    } catch (Exception e) {
                        throw new RuntimeException("can not exec method", e);
                    }
                }, anns.updatable.value(), anns.updatable.timeUnit());
            } else {
                final Schedule schedule = anns.schedule;
                if (schedule != null) {
                    final Integer value;
                    if (schedule.value() == -1) {
                        value = configInjector.getInstance(Key.get(Integer.class, Names.named(schedule.parameter())));
                    } else {
                        value = schedule.value();
                    }
                    final Runnable[] runnableLink = new Runnable[1];
                    runnableLink[0] = () -> {
                        try {
                            anns.method.invoke(i);
                        } catch (Exception e) {
                            log.error("Exception:" + e);
                            throw new RuntimeException("can not exec method", e);
                        }
                        taskProcessorProvider.get().schedule(runnableLink[0],
                                value, schedule.timeUnit());
                    };
                    taskProcessorProvider.get().schedule(runnableLink[0],
                            schedule.startImmediately() ? 0 : value, schedule.timeUnit());

                }
            }
        }
    }

    private static class Hook extends Thread {

        private List<Runnable> destroyList = new ArrayList<>();

        @Override
        public void run() {
            for (Runnable destroy : destroyList) {
                try {
                    destroy.run();
                } catch (Exception e) {
                    log.warn("Can not destroy", e);
                }
            }

        }
    }
}
