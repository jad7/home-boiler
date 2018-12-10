package com.jad.r4j.boiler;

import com.google.inject.*;
import com.google.inject.name.Names;
import com.jad.r4j.boiler.config.RaspberryConfig;
import com.jad.r4j.boiler.config.SystemConfig;
import com.jad.r4j.boiler.impl.MCP3208Controller;
import com.jad.r4j.boiler.impl.TaskProcessor;
import com.jad.r4j.boiler.impl.sensor.MCP3208TemperatureSensor;
import com.pi4j.component.relay.Relay;

import java.util.concurrent.TimeUnit;

public class Application {

	public static void main(String[] args) throws InterruptedException {
        /*int s = 0;
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                if (sum(i) == sum(j)) {
                    s++;
                }
            }
        }
        System.out.println("Count:" + s);

        int sum = 0;
        for (int i = 1; i < 10; i++) {
            sum += (i*i);
        }
        sum *= 2;
        sum+= 100;
        System.out.println("count2:" + sum);*/

        final Injector configInjector = Guice
                .createInjector(new SystemConfig());
        Injector injector = configInjector
                .createChildInjector(new RaspberryConfig(configInjector));
        final TaskProcessor taskProcessor = injector.getInstance(TaskProcessor.class);
        /*final  MCP3208TemperatureSensor ts = injector.getInstance(Key.get(MCP3208TemperatureSensor.class, Names.named("kitchenTemp")));
        Runnable[] runnables = new Runnable[1];
        runnables[0] = () -> {
            try {
                ts.schedule();
            } catch (Exception e) {
                e.printStackTrace();
            }
            taskProcessor.schedule(runnables[0], 1, TimeUnit.SECONDS);

        };
        taskProcessor.schedule(runnables[0], 0, TimeUnit.SECONDS);*/
        taskProcessor.processTasks();
        /*for (int i = 0; i < 1000; i++) {
            boilerRelay.toggle();
            TimeUnit.SECONDS.sleep(2);
        }*/
    }

    private static int sum(int j) {
        return (j % 10) + (j /10);
    }


}
