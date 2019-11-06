package com.jad.r4j.boiler.impl;

import java.beans.ConstructorProperties;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Singleton;

@Singleton
public class TaskProcessor {
   private PriorityQueue<TaskProcessor.Task> taskSet = new PriorityQueue();

   public TaskProcessor() {
   }

   public void schedule(Runnable runnable, long timeUnitValue, TimeUnit timeUnit) {
      long val = timeUnit.toMillis(timeUnitValue);
      long current = System.currentTimeMillis();
      long whenHaveToStart = val + current;
      TaskProcessor.Task task = new TaskProcessor.Task(runnable, whenHaveToStart);
      this.taskSet.offer(task);
   }

   public void scheduleRepitedForever(Runnable runnable, long timeUnitValue, TimeUnit timeUnit) {
      AtomicReference<Runnable> runnableAtomicReference = new AtomicReference();
      runnableAtomicReference.set(() -> {
         runnable.run();
         this.schedule((Runnable)runnableAtomicReference.get(), timeUnitValue, timeUnit);
      });
      this.schedule((Runnable)runnableAtomicReference.get(), timeUnitValue, timeUnit);
   }

   public void processTasks() throws InterruptedException {
      TaskProcessor.Task pool;
      for(; !Thread.currentThread().isInterrupted(); pool.taskRunnable.run()) {
         pool = (TaskProcessor.Task)this.taskSet.poll();
         long wait = pool.when - System.currentTimeMillis();
         if (wait > 0L) {
            Thread.sleep(wait);
         }
      }

   }

   private static class Task implements Comparable<TaskProcessor.Task> {
      private final Runnable taskRunnable;
      private final long when;

      public int compareTo(TaskProcessor.Task o) {
         return Long.compare(this.when, o.when);
      }

      @ConstructorProperties({"taskRunnable", "when"})
      public Task(Runnable taskRunnable, long when) {
         this.taskRunnable = taskRunnable;
         this.when = when;
      }
   }
}
