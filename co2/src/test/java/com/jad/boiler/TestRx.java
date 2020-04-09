package com.jad.boiler;

import com.jad.r4j.boiler.Main;
import io.reactivex.*;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.internal.disposables.EmptyDisposable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class TestRx {

    private static long start = System.currentTimeMillis();

    //For manual testing
    //@Test
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

    @Test
    public void testCombine() {
        log("Start");
        //Scheduler scheduler = new MyScheduler();
        Scheduler scheduler = Schedulers.from(Executors.newScheduledThreadPool(1));
        Observable<Float> s1 = Observable.interval(0,800, TimeUnit.MILLISECONDS, scheduler)
                .map(l -> {
                    log("sensor1");
                    Thread.sleep(100);
                    return (float) (Math.random() * 25);
                });

        final Ref<Map<Integer, Float>> ref = new Ref<>(new HashMap<>());

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
                .groupBy(f -> f.intValue() / 25)
                .flatMap(gr -> {
                    Integer key = gr.getKey();
                    return gr.map(val -> {
                        ref.t.put(key, val);
                        return ref.t;
                    });
                })

                .throttleLatest(1, TimeUnit.SECONDS, scheduler)
                .subscribeOn(scheduler)
                .doOnNext(v -> log(v.toString()))
                .blockingSubscribe();
    }

    @Data @AllArgsConstructor static class Ref<T> { T t; }

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
