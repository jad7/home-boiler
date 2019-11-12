package com.jad.r4j.boiler.utils;

import lombok.ToString;

import java.util.concurrent.TimeUnit;

public class RingBufferTimeserial {

   private final TimePoint[] array;
   private int size = 0;
   private int position = 0;


   public RingBufferTimeserial(int maxSize) {
      this.array = new TimePoint[maxSize];
      for (int i = 0; i < maxSize; i++) {
         array[i] = new TimePoint();
      }
   }



   public void add(double val) {
      add(System.currentTimeMillis(), val);
   }

   public void add(long time, double val) {
      if (this.position == this.array.length) {
         this.position = 0;
      }
      TimePoint timePoint = array[position];
      timePoint.time = time;
      timePoint.value = val;
      position++;
      if (size < array.length) {
         size++;
      }
   }

   public Double findMax(long fromTime) {
      int index = findIndex(fromTime);
      if (index < 0) {
         index = ~index;
      }
      if (index == size) {
         return null;
      }
      double max = Double.MIN_VALUE;
      for (int i = index; i < size; i++) {
         max = Math.max(max, array[toIndx(i)].value);
      }
      return max;
   }

   public Double findMax(int val, TimeUnit unit) {
      return findMax(System.currentTimeMillis() - unit.toMillis(val));
   }

   private int findIndex(long timeKey) {
      int low = 0;
      int high = size;

      while (low <= high) {
         int mid = (low + high) >>> 1;

         long midVal = array[toIndx(mid)].time;

         if (midVal < timeKey)
            low = mid + 1;
         else if (midVal > timeKey)
            high = mid - 1;
         else
            return mid; // key found
      }
      return -(low + 1);  // key not found.
   }

   private int toIndx(int ind) {
      return (ind + position) % size;
   }


   public Double avg(int forLast, TimeUnit timeUnit) {
      return avg(System.currentTimeMillis() - timeUnit.toMillis(forLast));
   }

   public Double avg(long time) {
      int index = findIndex(time);
      if (index < 0) {
         index = ~index;
      }
      double avg = 0;
      int count = size - index;
      if (count == 0) {
         return null;
      }
      for (int i = index; i < size; i++) {
         avg += array[toIndx(i)].value / count;
      }
      return avg;
   }

   public Double getLast() {
      if (position == 0) {
         return null;
      }
      return array[position - 1].value;
   }





   /*private Integer findNewMax() {
      Double max = this.valueExtractor.getValue(this.array[0]);
      Integer maxPostion = 0;

      for(int i = 1; i < this.array.length; ++i) {
         T t = this.array[i];
         if (t != null) {
            double value = this.valueExtractor.getValue(t);
            if (Double.compare(max, value) > 0) {
               maxPostion = i;
               max = value;
            }
         }
      }

      return maxPostion;
   }

   public double mean() {
      if (this.size == 0) {
         return 0.0D / 0.0;
      } else if (this.size < 50) {
         double sum = 0.0D;

         for(int i = 0; i < this.size; ++i) {
            sum += this.valueExtractor.getValue(this.array[i]);
         }

         return sum / (double)this.size;
      } else {
         BigDecimal sum = new BigDecimal(0);

         for(int i = 0; i < this.size; ++i) {
            sum = sum.add(new BigDecimal(this.valueExtractor.getValue(this.array[i])));
         }

         return sum.divide(new BigDecimal(this.size)).doubleValue();
      }
   }*/

   @ToString //For debug
   private static class TimePoint {
      long time;
      double value;
   }


}
