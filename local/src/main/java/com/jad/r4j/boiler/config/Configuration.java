package com.jad.r4j.boiler.config;

import com.jad.r4j.boiler.utils.Tuple;

import java.util.function.Consumer;

public interface Configuration {
    <T> void update(String key, Class<?> type, T value);

    void registerListener(String prefix, Consumer<Tuple<String, ?>> consumer);

    Configuration getConfigByPrefix(String prefix);

    boolean getBool(String s);

    int getInt(String b);

    double getDouble(String prop);

    String getStr(String name);
}
