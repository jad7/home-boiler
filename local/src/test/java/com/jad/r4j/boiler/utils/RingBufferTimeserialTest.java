package com.jad.r4j.boiler.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RingBufferTimeserialTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void findMaxLessSize() {
        for (int j = 0; j < 10_000; j++) {
            int maxSize = 10 + (int) (Math.random() * 490);
            RingBufferTimeserial rb = new RingBufferTimeserial(maxSize);
            int size = 5 + (int) (Math.random() * (maxSize - 10));
            List<Double> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                double val = Math.random() * 10000;
                list.add(val);
                rb.add(10_000 + (i * 1000) + (long)(Math.random() * 1000), val);
            }
            Double realMax = list.stream().max(Double::compare).get();
            double max = rb.findMax(9_999);
            Assert.assertEquals(realMax, max, 0);

            long range = 1 + (long) (Math.random() * (size - 2));
            Double realRangeMax = list.stream().skip(size - range).max(Double::compare).get();
            double rangeMax = rb.findMax(10_000 + ((size - range) * 1000));
            Assert.assertEquals(realRangeMax, rangeMax, Double.MIN_NORMAL);
        }

    }

    @Test
    public void findMaxOverflow() {
        RingBufferTimeserial rb = new RingBufferTimeserial(50);
        int size = (int) (50 + Math.random() * 50);
        List<Double> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            double val = Math.random() * 10000;
            list.add(val);
            rb.add(10_000 + (i * 1000) + (long)(Math.random() * 1000), val);
        }
        list = list.subList(list.size() - 50, list.size());
        Double realMax = list.stream().max(Double::compare).get();
        double max = rb.findMax(999);
        Assert.assertEquals(realMax, max, 0);

        long range = 1 + (long) (Math.random() * (list.size() - 2));
        Double realRangeMax = list.stream().skip(list.size() - range).max(Double::compare).get();
        double rangeMax = rb.findMax(10_000 + ((size - range) * 1000));
        Assert.assertEquals(realRangeMax, rangeMax, 0);

    }

    @Test
    public void findAvgOverflow() {
        RingBufferTimeserial rb = new RingBufferTimeserial(50);
        int size = (int) (50 + Math.random() * 50);
        List<Double> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            double val = Math.random() * 10000;
            list.add(val);
            rb.add(10_000 + (i * 1000) + (long) (Math.random() * 1000), val);
        }
        list = list.subList(list.size() - 50, list.size());
        Double realAvg = list.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
        double avg = rb.avg(999);
        Assert.assertEquals(realAvg, avg, 0.0001);
    }
}