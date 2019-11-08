package com.jad.r4j.boiler.config;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import java.io.*;
import java.util.*;

import com.jad.r4j.boiler.utils.NamedImpl;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Provider;

public class SystemConfig extends AbstractModule {
   public SystemConfig() {
   }

   public Properties loadFromYamlStream(InputStream inputStream) {
      Properties properties = new Properties();
      if (inputStream != null) {
         Yaml yaml = new Yaml();
         try (InputStream is = inputStream){
            TreeMap<String, Map<String, Object>> treeMap = yaml.loadAs(is, TreeMap.class);
            String propertiesString = toProperties(treeMap);
            properties.load(new ByteArrayInputStream(propertiesString.getBytes()));
         } catch (IOException io) {
            throw new RuntimeException("Exception on loading properties", io);
         }
      }
      return properties;
   }

   protected void configure() {
      Map<String, String> merged = new HashMap<>();

         try {
            merge(merged, loadFromYamlStream(this.getClass().getClassLoader().getResourceAsStream("application.yml")));

            File file = new File("application.yml");
            if (file.exists()) {
               merge(merged, loadFromYamlStream(new FileInputStream(file)));
            }

            merged.putAll(System.getenv());

            merge(merged, System.getProperties());

            final ConfigurationParent parentConfig = new ConfigurationParent(merged);
            bind(Configuration.class).toInstance(parentConfig);
            Binder binder = binder().skipSources(Names.class);
            Set<String> paths = new HashSet<>();
            for (Map.Entry<String, String> entry : merged.entrySet()) {
               String key = entry.getKey();
               paths.addAll(extractPaths(key));
               binder.bind(Key.get(String.class, new NamedImpl(key)))
                       .toProvider(() -> parentConfig.getStr(key))
               ;
            }
            for (String path : paths) {
               binder.bind(Key.get(String.class, new NamedImpl(path)))
                       .toProvider((Provider)() -> parentConfig.getConfigByPrefix(path))
                       .in(Scopes.SINGLETON);
            }


         } /*catch (Throwable var17) {
            throwable = var17;
            throw var17;
         } */ catch (FileNotFoundException e) {
            throw new RuntimeException("File not found", e);
         } /*finally {
            if (classPathProps != null) {
               if (throwable != null) {
                  try {
                     classPathProps.close();
                  } catch (Throwable var16) {
                     throwable.addSuppressed(var16);
                  }
               } else {
                  classPathProps.close();
               }
            }

         }*/

      /*} catch (IOException var19) {
         throw new RuntimeException("Exception on loading properties", var19);
      }*/
   }

   private Collection<? extends String> extractPaths(String key) {
      if (key == null || key.isEmpty()) {
         return Collections.emptyList();
      }
      String[] split = StringUtils.split(key, '.');
      if (split.length < 2) {
         return Collections.emptyList();
      }
      List<String> objects = new ArrayList<>(split.length - 1);
      for (int i = 0; i < split.length - 1; i++) {
         objects.add(split[i]);
      }
      return objects;
   }

   private void merge(Map<String, String> to, Properties from) {
      Iterator keyIterator = from.keySet().iterator();

      while(keyIterator.hasNext()) {
         Object o = keyIterator.next();
         to.put(o.toString(), from.get(o).toString());
      }

   }

   private String toProperties(TreeMap<String, Map<String, Object>> config) {
      StringBuilder sb = new StringBuilder();
      Iterator<String> keyIterator = config.keySet().iterator();

      while(keyIterator.hasNext()) {
         String key = keyIterator.next();
         Object value = config.get(key);
         if (value instanceof Map) {
            sb.append(toString(key, (Map)value));
         } else {
            sb.append(String.format("%s=%s%n", key, value.toString()));
         }
      }

      return sb.toString();
   }

   private String toString(String key, Map<String, Object> map) {
      StringBuilder sb = new StringBuilder();
      Iterator<String> keyIterator = map.keySet().iterator();

      while(keyIterator.hasNext()) {
         String mapKey = keyIterator.next();
         if (map.get(mapKey) instanceof Map) {
            sb.append(toString(String.format("%s.%s", key, mapKey), (Map)map.get(mapKey)));
         } else {
            sb.append(String.format("%s.%s=%s%n", key, mapKey, map.get(mapKey).toString()));
         }
      }

      return sb.toString();
   }
}
