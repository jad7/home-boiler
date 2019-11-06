package com.jad.r4j.boiler.utils;

import java.math.BigDecimal;

public class RingBuffer<T> {
   private static final RingBuffer.ValueExtractor<Object> defaultValueExtractor = (obj) -> {
      if (obj == null) {
         throw new NullPointerException();
      } else if (obj instanceof Number) {
         return ((Number)obj).doubleValue();
      } else if (obj instanceof RingBuffer.ValueContainer) {
         return ((RingBuffer.ValueContainer)obj).getValue();
      } else {
         throw new IllegalArgumentException("Object " + obj + " not supporting value extractor");
      }
   };
   private final T[] array;
   private final RingBuffer.ValueExtractor<? super T> valueExtractor;
   private int size = 0;
   private int position = 0;
   private Integer maxPosition;

   public RingBuffer(int maxSize) {
      this.array = (T[])(new Object[maxSize]);
      this.valueExtractor = defaultValueExtractor;
   }

   public RingBuffer(int maxSize, RingBuffer.ValueExtractor<T> valueExtractor) {
      this.array = (T[])(new Object[maxSize]);
      this.valueExtractor = valueExtractor;
   }

   public void add(T t) {
      if (this.position == this.array.length) {
         this.position = 0;
      }

      if (this.maxPosition == null) {
         this.maxPosition = 0;
      } else if (Double.compare(this.valueExtractor.getValue(this.array[this.maxPosition]), this.valueExtractor.getValue(t)) >= 0) {
         this.maxPosition = this.position;
      } else if (this.maxPosition == this.position) {
         this.maxPosition = this.findNewMax();
      }

      this.array[this.position] = t;
      ++this.position;
      if (this.size < this.array.length) {
         ++this.size;
      }

   }

   public T getMax() {
      return this.maxPosition == null ? null : this.array[this.maxPosition];
   }

   private Integer findNewMax() {
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
   }

   public interface ValueContainer {
      double getValue();
   }

   @FunctionalInterface
   public interface ValueExtractor<T> {
      double getValue(T var1);
   }
}
