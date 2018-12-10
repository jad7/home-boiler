package com.jad.r4j.boiler.config;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**

 * <p>
 * Developed by Grid Dynamics International, Inc. for the customer Art.com.
 * http://www.griddynamics.com
 * <p>
 * Classification level: Confidential
 * <p>
 * EXCEPT EXPRESSED BY WRITTEN WRITING, THIS CODE AND INFORMATION ARE PROVIDED "AS IS"
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * For information about the licensing and copyright of this document please
 * contact Grid Dynamics at info@griddynamics.com.
 *
 * @since 10/15/2018
 */
public class SystemConfig extends AbstractModule {

    @SuppressWarnings("unckeked")
    @Override
    protected void configure() {
        Map<String, String> merged = new HashMap<>();
        final Yaml yaml = new Yaml();
        final ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream resourceAsStream = classLoader.getResourceAsStream("application.yml")) {
            final TreeMap<String, Map<String, Object>> treeMap = yaml.loadAs(resourceAsStream, TreeMap.class);
            final String properties = toProperties(treeMap);
            final Properties properties1 = new Properties();
            properties1.load(new ByteArrayInputStream(properties.getBytes()));
            putAll(merged, properties1);
            merged.putAll(System.getenv());
            putAll(merged, System.getProperties());
            Names.bindProperties(binder(), merged);
            bind(Configuration.class).toInstance(new Configuration(merged));
        } catch (IOException e) {
            throw new RuntimeException("Exception on loading properties", e);
        }
    }

    private void putAll(Map<String, String> to, Properties from) {
        for (Object o : from.keySet()) {
            to.put(o.toString(), from.get(o).toString());
        }
    }

    private static String toProperties(TreeMap<String, Map<String, Object>> config) {

        StringBuilder sb = new StringBuilder();

        for (String key : config.keySet()) {

            final Object value = config.get(key);
            if (value instanceof Map) {
                sb.append(toString(key, (Map)value));
            } else {
                sb.append(String.format("%s=%s%n", key, value.toString()));
            }

            //sb.append(toString(key, map));
        }

        return sb.toString();
    }

    private static String toString(String key, Map<String, Object> map) {

        StringBuilder sb = new StringBuilder();

        for (String mapKey : map.keySet()) {

            if (map.get(mapKey) instanceof Map) {
                sb.append(toString(String.format("%s.%s", key, mapKey), (Map<String, Object>) map.get(mapKey)));
            } else {
                sb.append(String.format("%s.%s=%s%n", key, mapKey, map.get(mapKey).toString()));
            }
        }

        return sb.toString();
    }
}
