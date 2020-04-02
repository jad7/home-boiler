package com.jad.boiler.remote.service;


import com.google.common.collect.Lists;
import com.jad.boiler.remote.dao.StateHistoryDao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
public class HistoryService {

    public static String[] TYPES = {"ROOM_TEMP", "CO2"};

    @Autowired
    private StateHistoryDao stateHistoryDao;
    @Value("${com.jad.boiler.remote.job.aggregate.hours:3}")
    private int aggregateHours;
    private ScheduledExecutorService executorService;
    @Getter private final AtomicBoolean savingError = new AtomicBoolean(false);

    @PostConstruct
    public void schedule() {
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleWithFixedDelay(this::doWork, aggregateHours, aggregateHours, TimeUnit.HOURS);
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdownNow();
    }

    public void doWork() {
        try {
            List<StateHistoryDao.Point> points = stateHistoryDao.loadOldestNHours(aggregateHours);
            if (points != null && !points.isEmpty()) {
                Map<String, List<Float>> map = new HashMap<>();
                for (StateHistoryDao.Point point : points) {
                    map.computeIfAbsent(point.getType(), (k) -> new ArrayList<>()).add(point.getValue());
                }
                Map<String, Double> result = map.entrySet().stream()
                        .map(e -> new Tuple<>(e.getKey(), e.getValue().stream()
                                .mapToDouble(Float::doubleValue).average().orElse(Double.MIN_VALUE))
                        )
                        .peek(t -> t.setB(t.b == Double.MIN_VALUE ? null : t.b))
                        .collect(Collectors.toMap(Tuple::getA, Tuple::getB));
                stateHistoryDao.storeHistory(result, points.get(points.size() - 1).getTime());
                stateHistoryDao.removeOldestNHours(aggregateHours);
            }
        } catch (Exception e) {
            log.error("Can not save history", e);
            savingError.set(true);
        }
    }

    public void resetError() {
        savingError.set(false);
    }

    public List<StateHistoryDao.Point> getCurrent(String type, int resPoints) {
        List<StateHistoryDao.Point> data = stateHistoryDao.loadLastNHours(24, type);
        List<StateHistoryDao.Point> subData = data.subList(0, data.size() - 1);
        int size = subData.size();
        int perPoint = size / resPoints;
        List<StateHistoryDao.Point> result = Lists.partition(subData, perPoint).stream()
                .map(list -> {
                    OptionalDouble average = list.stream().mapToDouble(StateHistoryDao.Point::getValue).average();
                    Timestamp time = list.get(list.size() / 2).getTime();
                    return new StateHistoryDao.Point(type, (float) average.getAsDouble(), time);
                })
                .collect(Collectors.toList());
        result.add(data.get(data.size() - 1));
        return result;
    }

    public static void main(String[] args) {
        List<Integer> data = IntStream.range(0, 13).boxed().collect(Collectors.toList());
        data.add(14);
        System.out.println(Lists.partition(data.subList(0, data.size() - 1), 5));
    }

    @AllArgsConstructor @Data
    private static class Tuple<A, B> {private A a; private B b;}
}
