package com.jad.r4j.boiler.config;

import com.jad.r4j.boiler.utils.Tuple;

import java.util.NavigableMap;
import java.util.function.Consumer;

public class ConfigurationChild implements Configuration {
    private String prefix;
    private ConfigurationParent parent;

    public ConfigurationChild(String prefix, ConfigurationParent parent) {
        this.prefix = prefix.endsWith(".") ? prefix : prefix + ".";
        this.parent = parent;
    }

    @Override
    public <T> void update(String key, Class<?> type, T value) {
        parent.update(prefix + key, type, value);
    }

    @Override
    public void registerListener(String prefix, Consumer<Tuple<String, ?>> consumer) {
        parent.registerListener(this.prefix + prefix, consumer);
    }

    @Override
    public Configuration getConfigByPrefix(String prefix) {
        return parent.getConfigByPrefix(this.prefix + prefix);
    }

    @Override
    public NavigableMap<String, String> getAll() {
        return parent.getAll().subMap(prefix, true,prefix + Character.MAX_VALUE, true);
    }

    @Override
    public boolean getBool(String s) {
        return parent.getBool(prefix + s);
    }

    @Override
    public int getInt(String b) {
        return parent.getInt(prefix + b);
    }

    @Override
    public double getDouble(String prop) {
        return parent.getDouble(prefix + prop);
    }

    @Override
    public String getStr(String name) {
        return parent.getStr(prefix + name);
    }
}
