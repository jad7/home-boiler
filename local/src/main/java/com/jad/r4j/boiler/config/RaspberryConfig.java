package com.jad.r4j.boiler.config;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.ProvisionListener;
import com.jad.r4j.boiler.impl.DecisionMaker;
import com.jad.r4j.boiler.impl.DisplayController;
import com.jad.r4j.boiler.impl.DynamicConfigurationHolder;
import com.jad.r4j.boiler.impl.LisaLineUpdater;
import com.jad.r4j.boiler.impl.MCP3208Controller;
import com.jad.r4j.boiler.impl.RemoteServiceUpdater;
import com.jad.r4j.boiler.impl.TaskProcessor;
import com.jad.r4j.boiler.impl.sensor.AbstractTemprSensor;
import com.jad.r4j.boiler.impl.sensor.MCP3208TemperatureSensor;
import com.jad.r4j.boiler.impl.sensor.StubTemperatureSensor;
import com.jad.r4j.boiler.impl.sensor.TM1637Python;
import com.pi4j.component.relay.Relay;
import com.pi4j.component.relay.impl.GpioRelayComponent;
import com.pi4j.gpio.extension.mcp.MCP3208Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiMode;
import java.beans.ConstructorProperties;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaspberryConfig extends AbstractModule {
   private static final Logger log = LoggerFactory.getLogger(RaspberryConfig.class);
   private final Injector configInjector;
   private final RaspberryConfig.Hook hook = new RaspberryConfig.Hook();

   public RaspberryConfig(Injector configInjector) {
      this.configInjector = configInjector;
   }

   @Singleton
   @Provides
   public GpioController gpioController() {
      return GpioFactory.getInstance();
   }

   @Named("boilerMain")
   @Singleton
   @Provides
   public Relay boilerSwitcherRelay(@Named("components.relay.boiler.main.pin") int pinNum, GpioController gpioController) {
      Pin pin = RaspiPin.getPinByAddress(pinNum);
      GpioPinDigitalOutput digitalOutput = gpioController.provisionDigitalOutputPin(pin);
      return new GpioRelayComponent(digitalOutput);
   }

   @Singleton
   @Provides
   public MCP3208Controller mcp3208(@Named("components.mcp3208.spi") int spi, @Named("components.mcp3208.spi.speed") int spiSpeed, @Named("components.mcp3208.voltage") double voltage) throws IOException {
      SpiChannel spiCh = SpiChannel.getByNumber(spi);
      MCP3208Controller mcp3208Controller = new MCP3208Controller(spiCh, spiSpeed, SpiMode.MODE_0, voltage);
      log.info("MCP3208 configured on SPI: {}", spiCh);
      return mcp3208Controller;
   }

   @Provides
   @Named("analogInput0")
   public Integer mcpCH0(MCP3208Controller controller) {
      return controller.pinReaderSignal(MCP3208Pin.CH0);
   }

   @Provides
   @Named("analogInput1")
   public Integer mcpCH1(MCP3208Controller controller) {
      return controller.pinReaderSignal(MCP3208Pin.CH7);
   }

   @Singleton
   @Provides
   @Named("kitchenTemp")
   public AbstractTemprSensor kitchenSensor(@Named("analogInput0") Provider<Integer> input, MCP3208Controller controller, Configuration globalConfig) {
      if (globalConfig.getBool("components.mcp3208.ch0.enabled")) {
         Configuration config = globalConfig.getConfigByPrefix("components.mcp3208.ch0.params.");
         return new MCP3208TemperatureSensor(input, config.getInt("b"), config.getDouble("bTemperature"), config.getDouble("bResistance"), controller.getVoltage(), config.getInt("shoulderResistor"), controller.getMaxValue(), config.getBool("cacheValues"), "kitchenSensor");
      } else {
         return new StubTemperatureSensor();
      }
   }

   @Singleton
   @Provides
   @Named("boilerOutputTemp")
   public AbstractTemprSensor boilerSensor(@Named("analogInput1") Provider<Integer> input, MCP3208Controller controller, Configuration globalConfig) {
      if (globalConfig.getBool("components.mcp3208.ch1.enabled")) {
         Configuration config = globalConfig.getConfigByPrefix("components.mcp3208.ch1.params.");
         return new MCP3208TemperatureSensor(input, config.getInt("b"), config.getDouble("bTemperature"), config.getDouble("bResistance"), controller.getVoltage(), config.getInt("shoulderResistor"), controller.getMaxValue(), config.getBool("cacheValues"), "boilerSensor");
      } else {
         return new StubTemperatureSensor();
      }
   }

   @Singleton
   @Provides
   @Named("tm1637Dispay")
   public TM1637Python dispay(@Named("config.display.script.path") String path) {
      return new TM1637Python(new File(path));
   }

   protected void configure() {
      Runtime.getRuntime().addShutdownHook(this.hook);
      bindUpdatableSchedule();
      bind(DynamicConfigurationHolder.class);
      bind(DecisionMaker.class).asEagerSingleton();
      bind(DisplayController.class).asEagerSingleton();
      bind(LisaLineUpdater.class).asEagerSingleton();
      bind(RemoteServiceUpdater.class).asEagerSingleton();
   }

   private void bindUpdatableSchedule() {
      final Provider<TaskProcessor> taskProcessorProvider = this.getProvider(TaskProcessor.class);
      final Map<Class, RaspberryConfig.Anns> cache = new HashMap<>();
      final Matcher<Class> classMatcher = Matchers.inSubpackage("com.jad.r4j.boiler");
      this.bindListener(Matchers.any(), new ProvisionListener[]{new ProvisionListener() {
         public <T> void onProvision(ProvisionListener.ProvisionInvocation<T> provision) {
            T obj = provision.provision();
            if (obj instanceof Initiable) {
               try {
                  ((Initiable)obj).init();
               } catch (Exception var4) {
                  throw new RuntimeException("Can not init object: " + provision.getBinding().getKey(), var4);
               }
            }

            if (obj instanceof Destroyable) {
               List destroyList = RaspberryConfig.this.hook.destroyList;
               Destroyable var10001 = (Destroyable)obj;
               ((Destroyable)obj).getClass();
               destroyList.add(var10001);
            }

            RaspberryConfig.Anns anns = (RaspberryConfig.Anns)cache.computeIfAbsent(obj.getClass(), (typeLiteral) -> {
               if (!classMatcher.matches(typeLiteral)) {
                  return new RaspberryConfig.Anns(null, null, null);
               } else {
                  Method[] var3 = typeLiteral.getDeclaredMethods();
                  int var4 = var3.length;

                  for(int var5 = 0; var5 < var4; ++var5) {
                     Method method = var3[var5];
                     Updatable updateble = method.getDeclaredAnnotation(Updatable.class);
                     if (updateble != null) {
                        RaspberryConfig.log.info("Found @Updatable for class: {}", typeLiteral.getName());
                        return new RaspberryConfig.Anns(updateble, null, method);
                     }

                     Schedule schedule = method.getDeclaredAnnotation(Schedule.class);
                     if (schedule != null) {
                        RaspberryConfig.log.info("Found @Schedule for class: {}", typeLiteral.getName());
                        return new RaspberryConfig.Anns(null, schedule, method);
                     }
                  }

                  return new RaspberryConfig.Anns(null, null, null);
               }
            });
            RaspberryConfig.this.processSchedul(obj, anns, taskProcessorProvider);
         }
      }});
   }

   private <I> void processSchedul(I i, RaspberryConfig.Anns anns, Provider<TaskProcessor> taskProcessorProvider) {
      if (anns != null && anns.method != null) {
         if (anns.updatable != null) {
            (taskProcessorProvider.get()).schedule(() -> {
               try {
                  anns.method.invoke(i);
               } catch (Exception var3) {
                  throw new RuntimeException("can not exec method", var3);
               }
            }, anns.updatable.value(), anns.updatable.timeUnit());
         } else {
            Schedule schedule = anns.schedule;
            if (schedule != null) {
               Integer value;
               if (schedule.value() == -1) {
                  value = (Integer)this.configInjector.getInstance(Key.get((Class)Integer.class, Names.named(schedule.parameter())));
               } else {
                  value = schedule.value();
               }

               Runnable[] runnableLink = new Runnable[1];
               runnableLink[0] = () -> {
                  try {
                     anns.method.invoke(i);
                  } catch (Exception var7) {
                     log.error("Exception:" + var7);
                     throw new RuntimeException("can not exec method", var7);
                  }

                  taskProcessorProvider.get().schedule(runnableLink[0], (long)value, schedule.timeUnit());
               };
               taskProcessorProvider.get().schedule(runnableLink[0], schedule.startImmediately() ? 0L : (long)value, schedule.timeUnit());
            }
         }
      }

   }

   private static class Hook extends Thread {
      private List<Runnable> destroyList;

      private Hook() {
         this.destroyList = new ArrayList();
      }

      public void run() {
         Iterator var1 = this.destroyList.iterator();

         while(var1.hasNext()) {
            Runnable destroy = (Runnable)var1.next();

            try {
               destroy.run();
            } catch (Exception var4) {
               RaspberryConfig.log.warn("Can not destroy", var4);
            }
         }

      }
   }

   private static class Anns {
      Updatable updatable;
      Schedule schedule;
      Method method;

      @ConstructorProperties({"updatable", "schedule", "method"})
      public Anns(Updatable updatable, Schedule schedule, Method method) {
         this.updatable = updatable;
         this.schedule = schedule;
         this.method = method;
      }
   }
}
