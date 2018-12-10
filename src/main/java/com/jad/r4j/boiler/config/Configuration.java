package com.jad.r4j.boiler.config;

import com.google.inject.Singleton;

import java.util.*;

import static java.util.stream.Collectors.toMap;

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
 * @since 11/04/2018
 */
@Singleton
public class Configuration {
    public static Boolean debug;
    private NavigableMap<String, String> merged = new TreeMap<>();


    public Configuration(Map<String, String> props) {
        merged.putAll(props);
        if (debug == null) {
            debug = getBool("debug");
        }
    }

    public Map<String, String> getByPrefix(String prefix, boolean cut) {
        final String substring = prefix.substring(0, prefix.length() - 1);
        final char charTo = (char) (prefix.charAt(prefix.length() - 1) + 1);
        if (!cut) {
            return merged.subMap(prefix, substring + charTo);
        } else {
            return merged.subMap(prefix, substring + charTo).entrySet()
                    .stream().collect(toMap(k -> k.getKey().substring(prefix.length()),
                        Map.Entry::getValue));
        }
    }

    public Configuration getConfigByPrefix(String prefix) {
        return new Configuration(getByPrefix(prefix, true));
    }

    public boolean getBool(String s) {
        return Boolean.valueOf(merged.get(s));
    }

    public int getInt(String b) {
        return Integer.valueOf(merged.get(b));
    }

    public double getDouble(String prop) {
        return Double.valueOf(merged.get(prop));
    }

    public String getStr(String name) {
        return merged.get(name);
    }
}
