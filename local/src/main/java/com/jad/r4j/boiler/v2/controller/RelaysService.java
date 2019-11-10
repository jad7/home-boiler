package com.jad.r4j.boiler.v2.controller;


import com.jad.r4j.boiler.config.Configuration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.function.Consumer;

import static com.jad.r4j.boiler.config.Configuration.*;

@Singleton
public class RelaysService {

    private Relay boiler;
    private Relay pump;
    private Provider<Boolean> gasStat;
    private Configuration configuration;

    @Inject
    public RelaysService(
            com.pi4j.component.relay.Relay boiler,
            com.pi4j.component.relay.Relay pump,
            @Named("gasStat") Provider<Boolean> gasStat,
            Configuration configuration) {
        this.configuration = configuration;
        this.boiler = new AbstractRelay(boiler, RelayState.CLOSE, Relay::on,
                state -> configuration.update(BOILER_RELAY_STAT, String.class, state));
        this.pump = new AbstractRelay(pump, RelayState.CLOSE, Relay::off,
                state -> configuration.update(PUMP_RELAY_STAT, String.class, state));
        this.gasStat = gasStat;
    }

    public Relay boiler() {
        return boiler;
    }

    public Relay pump() {
        return pump;
    }

    public boolean isHeaterOn() {
        Boolean val = gasStat.get();
        configuration.update(GAS_STAT, String.class, val.toString().toUpperCase());
        return val;
    }


    private static class AbstractRelay implements Relay {
        private com.pi4j.component.relay.Relay piRelay;
        private RelayState onState;
        private Consumer<Relay> autoMethod;
        private Consumer<String> configUpdater;

        public AbstractRelay(com.pi4j.component.relay.Relay piRelay, RelayState onState,
                             Consumer<Relay> autoMethod, Consumer<String> configUpdater) {
            this.piRelay = piRelay;
            this.onState = onState;
            this.autoMethod = autoMethod;
            this.configUpdater = configUpdater;
        }

        @Override
        public void on() {
            onState.accept(piRelay);
            configUpdater.accept("ON");
        }

        @Override
        public void off() {
            (onState == RelayState.OPEN ? RelayState.CLOSE : RelayState.OPEN).accept(piRelay);
            configUpdater.accept("OFF");
        }

        @Override
        public void auto() {
            autoMethod.accept(this);
            configUpdater.accept("AUTO");
        }


    }

    enum RelayState implements Consumer<com.pi4j.component.relay.Relay> {
        OPEN {
            @Override
            public void accept(com.pi4j.component.relay.Relay relay) {
                if (relay.isClosed()) {
                    relay.open();
                }
            }
        },
        CLOSE {
            @Override
            public void accept(com.pi4j.component.relay.Relay relay) {
                if (relay.isOpen()) {
                    relay.close();
                }
            }
        }
    }

}
