package com.jad.r4j.boiler.integration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.jad.r4j.boiler.impl.MCP3208Controller;
import com.jad.r4j.boiler.impl.sensor.MHZ19;
import com.pi4j.component.relay.Relay;
import com.pi4j.component.relay.impl.GpioRelayComponent;
import com.pi4j.gpio.extension.mcp.MCP3208Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiMode;
import lombok.extern.slf4j.Slf4j;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;

@Slf4j
public class TestModule extends AbstractModule {

    @Named("boilerRelay") @Singleton
    @Provides
    public Relay boilerRelay() {
        return Mockito.mock(Relay.class);
    }

    @Named("pumpRelay") @Singleton @Provides
    public Relay pumpRelay() {
        return Mockito.mock(Relay.class);
    }

    @Singleton @Provides
    public MCP3208Controller mcp3208(@Named("components.mcp3208.spi") int spi,
                                     @Named("components.mcp3208.spi.speed") int spiSpeed,
                                     @Named("components.mcp3208.voltage") double voltage) throws IOException {
        MCP3208Controller mcp3208Controller = Mockito.mock(MCP3208Controller.class);
        BDDMockito.when(mcp3208Controller.pinReaderSignal(Mockito.any())).then((inv) -> Math.random() * 256);
        return mcp3208Controller;
    }

    @Override
    protected void configure() {
        MHZ19 mock = Mockito.mock(MHZ19.class);
        try {
            BDDMockito.when(mock.read()).then((inv) -> (int)(Math.random() * 2000));
        } catch (IOException e) {
            e.printStackTrace();
        }
        bind(MHZ19.class).toInstance(mock);
    }

    @Singleton
    @Provides
    @Named("gasStatInput")
    public GpioPinDigitalInput gasStatInput() {
        GpioPinDigitalInput mock = Mockito.mock(GpioPinDigitalInput.class);
        BDDMockito.when(mock.isHigh()).then((inv) -> Math.random() > 0.5);
        return mock;
    }

}
