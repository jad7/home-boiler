package com.jad.r4j.boiler.config;

import com.jad.r4j.boiler.utils.Tuple;

import java.util.function.Consumer;

public interface Configuration {
    String CURRENT_ROOM_TEMP_KEY = "currentRoomTmp";
    String CURRENT_WATER_TEMP_KEY = "currentWaterTmp";
    String CURRENT_STATE_KEY = "currentStat";
    String CURRENT_MODE_KEY = "currentMod";
    String BOILER_RELAY_STAT = "boilerStat";
    String PUMP_RELAY_STAT = "pumpStat";
    String GAS_STAT = "gasStat";


    <T> void update(String key, Class<?> type, T value);

    void registerListener(String prefix, Consumer<Tuple<String, ?>> consumer);

    Configuration getConfigByPrefix(String prefix);

    boolean getBool(String s);

    int getInt(String b);

    double getDouble(String prop);

    String getStr(String name);
}
