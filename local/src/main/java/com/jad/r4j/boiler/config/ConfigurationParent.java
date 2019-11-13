package com.jad.r4j.boiler.config;

import com.google.inject.Singleton;
import com.jad.r4j.boiler.utils.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.jad.r4j.boiler.utils.Functions.*;

@Singleton
@Slf4j
public class ConfigurationParent implements Configuration {
   public static Boolean debug;
   private final NavigableMap<String, String> merged = new TreeMap<>();
   private final NavigableMap<String, List<Consumer<Tuple<String, ?>>>> listeners = new TreeMap<>();

   public ConfigurationParent(Map<String, String> props) {
      this.merged.putAll(props);
      if (debug == null) {
         debug = this.getBool("debug");
      }
   }

   @Override
   public <T> void update(String key, Class<?> type, T value) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(value);
      String newValue = String.valueOf(value);
      String current = merged.get(key);
      if (current != null && current.equals(newValue)) {
         if (debug) {
            log.debug("Key {} has not updated, value the same", key);
         }
         return;
      } else {
         merged.put(key, newValue);
      }
      String[] split = StringUtils.split(key, '.');
      Tuple<String, ?> tuple = new Tuple<>(key, value);
      listeners.getOrDefault("", Collections.emptyList())
              .forEach(curry2C(Consumer::accept, tuple));
      StringBuilder stringBuilder = new StringBuilder();
      int index = 0;
      do {
         listeners.getOrDefault(stringBuilder.toString(), Collections.emptyList())
                 .forEach(curry2C(Consumer::accept, tuple));
         if (stringBuilder.length() != 0) {
            stringBuilder.append('.');
         }
         stringBuilder.append(split[index]);
         index++;
      } while (index != split.length);
      listeners.getOrDefault(stringBuilder.toString(), Collections.emptyList())
              .forEach(curry2C(Consumer::accept, tuple));

   }

   @Override
   public void registerListener(String prefix, Consumer<Tuple<String, ?>> consumer) {
      listeners.computeIfAbsent(prefix, supToFunc(ArrayList::new)).add(consumer);
   }

   /*public Map<String, String> getByPrefix(String prefix, boolean cut) {
      String substring = prefix.substring(0, prefix.length() - 1);
      char charTo = (char)(prefix.charAt(prefix.length() - 1) + 1);
      return (!cut ? this.merged.subMap(prefix, substring + charTo) : this.merged.subMap(prefix, substring + charTo)
              .entrySet()
              .stream()
              .collect(Collectors.toMap((k) ->  (k.getKey()).substring(prefix.length())
      , Entry::getValue)));
   }*/

   @Override
   public Configuration getConfigByPrefix(String prefix) {
      return new ConfigurationChild(prefix, this);
      //return new ConfigurationParent(this.getByPrefix(prefix, true));
   }

   @Override
   public NavigableMap<String, String> getAll() {
      return Collections.unmodifiableNavigableMap(merged);
   }

   @Override
   public boolean getBool(String s) {
      return Boolean.parseBoolean(this.merged.get(s));
   }

   @Override
   public int getInt(String b) {
      return Integer.parseInt(this.merged.get(b));
   }

   @Override
   public double getDouble(String prop) {
      return Double.parseDouble(this.merged.get(prop));
   }

   @Override
   public String getStr(String name) {
      return this.merged.get(name);
   }
}
