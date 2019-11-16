package com.jad.r4j.boiler.v2.controller;


import com.jad.r4j.boiler.config.Configuration;
import com.jad.r4j.boiler.impl.TaskProcessor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class BoilerController {

    private final RelaysService relaysService;
    private final TemperatureService temperatureService;
    private final Configuration configuration;
    private final DisplayService displayService;

    @Inject
    public BoilerController(RelaysService relaysService,
                            TemperatureService temperatureService,
                            TaskProcessor taskProcessor,
                            Configuration configuration,
                            DisplayService displayService) {
        this.relaysService = relaysService;
        this.temperatureService = temperatureService;
        this.configuration = configuration;
        mode = Mode.valueOf(configuration.getStr(Configuration.CURRENT_MODE_KEY));
        this.displayService = displayService;
        configuration.update(Configuration.CURRENT_MODE_VALUES, String.class,
                Stream.of(Mode.values()).map(Mode::name).collect(Collectors.joining(",")));
        configuration.registerListener(Configuration.CURRENT_MODE_KEY,
                tuple -> changeMode(Mode.valueOf(String.valueOf(tuple.getB()))));
        taskProcessor.scheduleRepeatable(this::onControl, 10, TimeUnit.SECONDS);
    }

    private Mode mode;

    private Stat stat;

    private void onControl() {
        getMode().onStatus(this);
    }

    public void changeMode(Mode mode) {
        if (mode != this.mode) {
            this.mode = mode;
            configuration.update(Configuration.CURRENT_MODE_KEY, String.class, mode.name());
            this.mode.onSet(this);
            //displayService.showNotification("");// TODO

        }
    }

    private Mode getMode() {
        return mode;
    }

    private Stat getState() {
        if (stat == null) {
            detectStatus();
        }
        return stat;
    }

    private void changeState(Stat stat) {
        if (this.stat != stat) {
            this.stat = stat;
            configuration.update(Configuration.CURRENT_STATE_KEY, String.class, stat.name());
            stat.onSet(this);
        }
    }

    public enum Mode {
        AUTO {
            @Override
            void onStatus(BoilerController controller) {
                controller.getState().onStatus(controller);
            }

            @Override
            void onSet(BoilerController controller) {
                controller.detectStatus();
                onStatus(controller);
            }
        },
        MANUAL {
            @Override
            void onStatus(BoilerController controller) {

            }

            @Override
            void onSet(BoilerController controller) {
                controller.relays().boiler().auto();
                controller.relays().pump().auto();
            }
        },
        DISABLED {
            @Override
            void onStatus(BoilerController controller) {

            }

            @Override
            void onSet(BoilerController controller) {
                controller.relays().boiler().off();
                controller.relays().pump().auto();
            }
        }
        ;

        abstract void onStatus(BoilerController controller);
        abstract void onSet(BoilerController controller);
    }

    private RelaysService relays() {
        return relaysService;
    }

    private void detectStatus() {
        if (isHeaterOn()) {
            stat = Stat.HEATING;
        } else {
            stat = Stat.COOLING_DOWN;
        }
        stat.onSet(this);
    }

    public enum Stat {

        HEATING {
            @Override
            void onStatus(BoilerController controller) {
                TemperatureService temperatureService = controller.temperatureService;
                if (!controller.isHeaterOn()) {
                    controller.changeState(COOLING_DOWN);
                } else {
                    if (temperatureService.getExpectedNowRange().max() <= temperatureService.getAvgRoomTemperature(1, TimeUnit.MINUTES)) {
                        controller.changeState(COOLING_DOWN);
                    }
                }
            }

            @Override
            void onSet(BoilerController controller) {
                controller.relays().boiler().auto();
                controller.relays().pump().auto();
            }
        },

        COOLING_DOWN {
            @Override
            void onStatus(BoilerController controller) {
                TemperatureService temperatureService = controller.temperatureService;
                if (temperatureService.getExpectedNowRange().max() <= temperatureService.getAvgRoomTemperature(3, TimeUnit.MINUTES)) {
                    controller.changeState(TURNED_OFF);
                } else if (temperatureService.isRadiatorTempLow()) {
                    controller.changeState(HEATING);
                }
            }

            @Override
            void onSet(BoilerController controller) {
                controller.relays().boiler().off();
                controller.relays().pump().on();
            }
        },

        TURNED_OFF {
            @Override
            void onStatus(BoilerController controller) {
                TemperatureService temperatureService = controller.temperatureService;
                if (temperatureService.getExpectedNowRange().min() >= temperatureService.getAvgRoomTemperature(1, TimeUnit.MINUTES)) {
                    controller.changeState(HEATING);
                }
            }

            @Override
            void onSet(BoilerController controller) {
                controller.relays().boiler().off();
                controller.relays().pump().auto();
            }
        },


        ;

        abstract void onStatus(BoilerController controller);
        abstract void onSet(BoilerController controller);
    }

    private boolean isHeaterOn() {
        return relaysService.isHeaterOn();
    }


}
