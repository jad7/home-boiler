package com.jad.r4j.boiler.utils;

import java.math.BigDecimal;

/**
 * @since 11/04/2018
 */
public class RingBuffer<T> {
    private static final ValueExtractor<Object> defaultValueExtractor = obj -> {
        if (obj == null) {
            throw new NullPointerException();
        } else if (obj instanceof Number) {
            return ((Number)obj).doubleValue();
        } else if (obj instanceof ValueContainer) {
            return ((ValueContainer) obj).getValue();
        } else {
            throw new IllegalArgumentException("Object " + obj + " not supporting value extractor");
        }
    };
    private final T[] array;
    private final ValueExtractor<? super T> valueExtractor;
    private int size = 0;
    private int position = 0;
    private Integer maxPosition;

    public RingBuffer(int maxSize) {
        array = (T[])new Object[maxSize];
        valueExtractor = defaultValueExtractor;
    }

    public RingBuffer(int maxSize, ValueExtractor<T> valueExtractor) {
        array = (T[])new Object[maxSize];
        this.valueExtractor = valueExtractor;
    }

    public void add(T t) {
        if (position == array.length) {
            position = 0;
        }
        if (maxPosition == null) {
            maxPosition = 0;
        } else {
            if (Double.compare(valueExtractor.getValue(array[maxPosition]), valueExtractor.getValue(t)) >= 0) {
                maxPosition = position;
            } else if (maxPosition == position) {
                maxPosition = findNewMax();
            }
        }
        array[position] = t;
        position++;
        if (size < array.length) {
            size++;
        }
    }

    public T getMax() {
        if (maxPosition == null) {
            return null;
        }
        return array[maxPosition];
    }

    private Integer findNewMax() {
        Double max = valueExtractor.getValue(array[0]);
        Integer maxPostion = 0;
        for (int i = 1; i < array.length; i++) {
            T t = array[i];
            if (t != null) {
                final double value = valueExtractor.getValue(t);
                if (Double.compare(max, value) > 0) {
                    maxPostion = i;
                    max = value;
                }
            }
        }
        return maxPostion;
    }

    public double mean() {
        if (size == 0) {
            return Double.NaN;
        }
        if (size < 50) {
            double sum = 0;
            for (int i = 0; i < size; i++) {
                sum += valueExtractor.getValue(array[i]);
            }
            return sum / size;
        } else {
            BigDecimal sum = new BigDecimal(0);
            for (int i = 0; i < size; i++) {
                sum = sum.add(new BigDecimal(valueExtractor.getValue(array[i])));
            }
            return sum.divide(new BigDecimal(size)).doubleValue();
        }
    }

    @FunctionalInterface
    public interface ValueExtractor<T> {
        double getValue(T obj);
    }

    public interface ValueContainer {
        double getValue();
    }


}
