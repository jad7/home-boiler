package com.jad.r4j.boiler.v2.controller;

import com.jad.r4j.boiler.config.Configuration;
import com.jad.r4j.boiler.impl.TaskProcessor;
import com.jad.r4j.boiler.impl.sensor.AbstractTemprSensor;
import com.jad.r4j.boiler.utils.Range;
import com.jad.r4j.boiler.utils.RingBufferTimeserial;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

import static com.jad.r4j.boiler.config.Configuration.CURRENT_ROOM_TEMP_KEY;
import static com.jad.r4j.boiler.config.Configuration.CURRENT_WATER_TEMP_KEY;
import static com.jad.r4j.boiler.utils.Functions.round;

@Singleton
public class TemperatureService {

    private final RingBufferTimeserial roomTS;
    private final RingBufferTimeserial waterTS;

    @Inject
    public TemperatureService(final @Named("roomTemp") AbstractTemprSensor roomSensor,
                              final @Named("waterTemp") AbstractTemprSensor waterSensor,
                              @Named("config.temperature") Configuration configuration,
                              DisplayService displayService,
                              TaskProcessor taskProcessor) {

        int refreshIntervalMs = configuration.getInt("refresh.ms");
        long bufferTimeMin = configuration.getInt("timeline.buffer.minutes");
        int bucketsNumber = (int)(bufferTimeMin * 60 * 60 * 1000 / refreshIntervalMs);
        roomTS = new RingBufferTimeserial(bucketsNumber);
        waterTS = new RingBufferTimeserial(bucketsNumber);
        taskProcessor.scheduleRepeatable(() -> {
            roomTS.add(roomSensor.getTemperatureRounded());
            waterTS.add(waterSensor.getTemperatureRounded());
        }, refreshIntervalMs, TimeUnit.MILLISECONDS);
        taskProcessor.scheduleRepeatable(() -> {
            configuration.update(CURRENT_ROOM_TEMP_KEY, Double.class,
                    round(roomTS.avg(10, TimeUnit.SECONDS)));
        }, 10, TimeUnit.SECONDS);
        taskProcessor.scheduleRepeatable(() -> {
            configuration.update(CURRENT_WATER_TEMP_KEY, Double.class,
                    round(waterTS.avg(10, TimeUnit.SECONDS)));
        }, 10, TimeUnit.SECONDS);

        displayService.addStatic(() -> {
            String str = configuration.getStr(CURRENT_ROOM_TEMP_KEY);
            if (StringUtils.isBlank(str)) {
                return "Un--\u2103C";
            }
            return str + "\u2103C";
        });
    }

    public Range<Double> getExpectedNowRange() {
        //TODO implement
        return null;
    }

    public Double getAvgRoomTemperature(int forLast, TimeUnit timeUnit) {
        return roomTS.avg(forLast, timeUnit);
    }

    public boolean isRadiatorTempHigh() {

        //TODO
        return false;
    }

    public boolean isRadiatorTempLow() {
        return false;
    }
}
