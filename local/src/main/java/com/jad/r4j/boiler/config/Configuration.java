package com.jad.r4j.boiler.config;

import com.jad.r4j.boiler.utils.Tuple;

import java.util.NavigableMap;
import java.util.function.Consumer;

public interface Configuration {
    String CURRENT_ROOM_TEMP_KEY = "dynamic.currentRoomTmp";
    String CURRENT_WATER_TEMP_KEY = "dynamic.currentWaterTmp";
    String CURRENT_STATE_KEY = "dynamic.currentStat";
    String CURRENT_MODE_KEY = "dynamic.currentMod";
    String BOILER_RELAY_STAT = "dynamic.boilerStat";
    String PUMP_RELAY_STAT = "dynamic.pumpStat";
    String CURRENT_CO2_STAT = "dynamic.currentCO2";
    String GAS_STAT = "dynamic.gasStat";


    <T> void update(String key, Class<?> type, T value);

    void registerListener(String prefix, Consumer<Tuple<String, ?>> consumer);

    Configuration getConfigByPrefix(String prefix);

    NavigableMap<String, String> getAll();

    boolean getBool(String s);

    int getInt(String b);

    double getDouble(String prop);

    String getStr(String name);
}
