package com.jad.r4j.boiler.v2.controller;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

@Singleton
public class RelaysService {

    private Relay boiler;
    private Relay pump;

    @Inject
    public RelaysService(
            com.pi4j.component.relay.Relay boiler,
            com.pi4j.component.relay.Relay pump) {
        this.boiler = new AbstractRelay(boiler, RelayState.CLOSE, Relay::on);
        this.pump = new AbstractRelay(pump, RelayState.CLOSE, Relay::off);
    }

    public Relay boiler() {
        return boiler;
    }

    public Relay pump() {
        return pump;
    }


    private static class AbstractRelay implements Relay {
        private com.pi4j.component.relay.Relay piRelay;
        private RelayState onState;
        private Consumer<Relay> autoMethod;

        public AbstractRelay(com.pi4j.component.relay.Relay piRelay, RelayState onState, Consumer<Relay> autoMethod) {
            this.piRelay = piRelay;
            this.onState = onState;
            this.autoMethod = autoMethod;
        }

        @Override
        public void on() {
            onState.accept(piRelay);
        }

        @Override
        public void off() {
            (onState == RelayState.OPEN ? RelayState.CLOSE : RelayState.OPEN).accept(piRelay);
        }

        @Override
        public void auto() {
            autoMethod.accept(this);
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
