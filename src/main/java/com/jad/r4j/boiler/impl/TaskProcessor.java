package com.jad.r4j.boiler.impl;

import lombok.AllArgsConstructor;

import javax.inject.Singleton;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
 * @since 11/03/2018
 */
@Singleton
public class TaskProcessor {

    private PriorityQueue<Task> taskSet = new PriorityQueue<>();

    public void schedule(Runnable runnable, long timeUnitValue, TimeUnit timeUnit) {
        final long val = timeUnit.toMillis(timeUnitValue);
        final long current = System.currentTimeMillis();
        long whenHaveToStart = val + current;
        final Task task = new Task(runnable, whenHaveToStart);
        taskSet.offer(task);
    }

    public void scheduleRepitedForever(Runnable runnable, long timeUnitValue, TimeUnit timeUnit) {
        AtomicReference<Runnable> runnableAtomicReference = new AtomicReference<>();
        runnableAtomicReference.set(() -> {
            runnable.run();
            schedule(runnableAtomicReference.get(), timeUnitValue, timeUnit);
        });
        schedule(runnableAtomicReference.get(), timeUnitValue, timeUnit);
    }

    public void processTasks() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            final Task pool = taskSet.poll();
            final long wait = pool.when - System.currentTimeMillis();
            if (wait > 0) {
                Thread.sleep(wait);
            }
            pool.taskRunnable.run();
        }

    }

    @AllArgsConstructor
    private static class Task implements Comparable<Task> {
        private final Runnable taskRunnable;
        private final long when;

        @Override
        public int compareTo(Task o) {
            return Long.compare(when, o.when);
        }
    }
}
