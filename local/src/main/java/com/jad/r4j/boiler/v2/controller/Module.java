package com.jad.r4j.boiler.v2.controller;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.jad.r4j.boiler.config.Configuration;
import com.jad.r4j.boiler.impl.MCP3208Controller;
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
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

@Slf4j
public class Module extends AbstractModule {

    @Named("boilerRelay") @Singleton @Provides
    public Relay boilerRelay(@Named("components.relay.boiler.main.pin") int pinNum, GpioController gpioController) {
        Pin pin = RaspiPin.getPinByAddress(pinNum);
        GpioPinDigitalOutput digitalOutput = gpioController.provisionDigitalOutputPin(pin);
        return new GpioRelayComponent(digitalOutput);
    }

    @Named("pumpRelay") @Singleton @Provides
    public Relay pumpRelay(@Named("components.relay.boiler.pump.pin") int pinNum, GpioController gpioController) {
        Pin pin = RaspiPin.getPinByAddress(pinNum);
        GpioPinDigitalOutput digitalOutput = gpioController.provisionDigitalOutputPin(pin);
        return new GpioRelayComponent(digitalOutput);
    }

    @Singleton @Provides
    public MCP3208Controller mcp3208(@Named("components.mcp3208.spi") int spi, @Named("components.mcp3208.spi.speed") int spiSpeed, @Named("components.mcp3208.voltage") double voltage) throws IOException {
        SpiChannel spiCh = SpiChannel.getByNumber(spi);
        MCP3208Controller mcp3208Controller = new MCP3208Controller(spiCh, spiSpeed, SpiMode.MODE_0, voltage);
        log.info("MCP3208 configured on SPI: {}", spiCh);
        return mcp3208Controller;
    }

    @Provides @Named("analogInput0")
    public Integer mcpCH0(MCP3208Controller controller) {
        return controller.pinReaderSignal(MCP3208Pin.CH0);
    }

    @Provides @Named("analogInput1")
    public Integer mcpCH1(MCP3208Controller controller) {
        return controller.pinReaderSignal(MCP3208Pin.CH7);
    }

    @Singleton @Provides @Named("roomTemp")
    public AbstractTemprSensor kitchenSensor(@Named("analogInput0") Provider<Integer> input, MCP3208Controller controller, Configuration globalConfig) {
        if (globalConfig.getBool("components.mcp3208.ch0.enabled")) {
            Configuration config = globalConfig.getConfigByPrefix("components.mcp3208.ch0.params.");
            return new MCP3208TemperatureSensor(input, config.getInt("b"), config.getDouble("bTemperature"), config.getDouble("bResistance"), controller.getVoltage(), config.getInt("shoulderResistor"), controller.getMaxValue(), config.getBool("cacheValues"), "kitchenSensor");
        } else {
            return new StubTemperatureSensor();
        }
    }

    @Singleton
    @Provides
    @Named("waterTemp")
    public AbstractTemprSensor boilerSensor(@Named("analogInput1") Provider<Integer> input, MCP3208Controller controller, Configuration globalConfig) {
        if (globalConfig.getBool("components.mcp3208.ch1.enabled")) {
            Configuration config = globalConfig.getConfigByPrefix("components.mcp3208.ch1.params.");
            return new MCP3208TemperatureSensor(input, config.getInt("b"), config.getDouble("bTemperature"), config.getDouble("bResistance"), controller.getVoltage(), config.getInt("shoulderResistor"), controller.getMaxValue(), config.getBool("cacheValues"), "boilerSensor");
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

    @Provides
    @Named("gasStat")
    public Provider<Boolean> gasStat(@Named("components.ac.stat.pin") int pinNum, GpioController gpioController) {
        Pin pin = RaspiPin.getPinByAddress(pinNum);
        GpioPinDigitalInput digitalInput = gpioController.provisionDigitalInputPin(pin);
        return digitalInput::isHigh;
    }


    @Override
    protected void configure() {

    }

}
