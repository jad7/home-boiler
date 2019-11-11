package com.jad.r4j.boiler.v2.controller;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Singleton
public class Lifecycle {
    private final List<Runnable> initableList = new ArrayList<>();
    private final List<Runnable> destroyableList = new ArrayList<>();

    public Lifecycle() {
        Runtime.getRuntime().addShutdownHook(new Hook());
    }

    public void addInit(Runnable initable) {
        initableList.add(initable);
    }

    public void addDestroyable(Runnable destr) {
        destroyableList.add(destr);
    }

    private class Hook extends Thread {

        private Hook() {
        }

        public void run() {
            Iterator<Runnable> itr = destroyableList.iterator();

            while(itr.hasNext()) {
                Runnable destroy = itr.next();

                try {
                    destroy.run();
                } catch (Exception ex) {
                    log.warn("Can not destroy", ex);
                }
            }

        }
    }
}
