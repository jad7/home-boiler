package com.jad.r4j.boiler.v2.controller;


import java.util.concurrent.TimeUnit;

public class BoilerController {

    private RelaysService relaysService;
    private TemperatureService temperatureService;

    public BoilerController(RelaysService relaysService, TemperatureService temperatureService) {
        this.relaysService = relaysService;
        this.temperatureService = temperatureService;
    }

    private Mode mode;
    private Stat stat;

    public void onControl() {
        getMode().onStatus(this);
    }

    public void changeMode(Mode mode) {
        if (mode != this.mode) {
            this.mode = mode;
            this.mode.onSet(this);
        }
    }

    private Mode getMode() {
        return mode;
    }

    private Stat getState() {
        return stat;
    }

    private void changeState(Stat stat) {
        if (this.stat != stat) {
            this.stat = stat;
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
        };

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
                if (!controller.isHeaterOn() && temperatureService.isRadiatorTempHigh()) {
                    controller.changeState(COOLING_DOWN);
                } else {
                    if (temperatureService.getExpectedNowRange().max() <= temperatureService.getAvgRoomTemperature(3, TimeUnit.MINUTES)) {
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
        };

        abstract void onStatus(BoilerController controller);
        abstract void onSet(BoilerController controller);
    }

    private boolean isHeaterOn() {
        //TODO impl
        return false;
    }


}
