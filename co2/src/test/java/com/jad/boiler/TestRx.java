package com.jad.boiler;

import io.reactivex.*;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.internal.disposables.EmptyDisposable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class TestRx {

    private static long start = System.currentTimeMillis();

    @Test
    public void testRX() {
        log("Start");
        //Scheduler scheduler = new MyScheduler();
        Scheduler scheduler = Schedulers.from(Executors.newScheduledThreadPool(1));
        Observable<Float> s1 = Observable.interval(0,800, TimeUnit.MILLISECONDS, scheduler)
                .map(l -> {
                    log("sensor1");
                    Thread.sleep(100);
                    return (float) (Math.random() * 25);
                });

        Observable<Float> s2 = Observable.interval(0,500, TimeUnit.MILLISECONDS, scheduler)
                .map(l -> {
                    log("sensor2");
                    Thread.sleep(100);
                    return (float) (Math.random() * 25 + 25);
                });

        Observable.fromArray(s1, s2)
                .flatMap(t -> t, false, 2, 1)
                .doOnNext(s -> {
                    //log("Consumed" + s);
                    Thread.sleep(100);
                })
                .subscribeOn(scheduler)
                .blockingSubscribe();



    }

    private static class MyScheduler extends Scheduler {
        private static Scheduler scheduler = Schedulers.trampoline();

        @Override
        public Worker createWorker() {
            return scheduler.createWorker();
        }

        @NonNull
        @Override
        public Disposable scheduleDirect(@NonNull Runnable run) {
            return scheduler.scheduleDirect(run);
        }

        @NonNull
        @Override
        public Disposable scheduleDirect(@NonNull Runnable run, long delay, TimeUnit unit) {
            return scheduler.scheduleDirect(run, delay, unit);
        }
    }

    private static void log(String s) {
        long currentTimeMillis = System.currentTimeMillis();
        System.out.println((currentTimeMillis - start)  + ": "
                + Thread.currentThread().getName() + ": " + s);
        start = currentTimeMillis;
    }
}
