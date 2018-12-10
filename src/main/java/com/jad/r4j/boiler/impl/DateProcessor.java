package com.jad.r4j.boiler.impl;

import com.jad.r4j.boiler.dto.BoilerSchedule;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

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
 * @since 11/11/2018
 */

public class DateProcessor {
    private static final Object WORKING = new Object();
    private static final Object WEEKEND = new Object();
    public static final Comparator<Point> POINT_COMPARATOR = Comparator.comparing(Point::getMinute);

    private static final int SATURDAY = 6;
    private static final int SUNDAY = 7;
    private static Set<Integer> weekendDaysList = new HashSet<>(Arrays.asList(SATURDAY, SUNDAY));


    private Map<Object, List<Point>> schedules = new HashMap<>();

    public DateProcessor(BoilerSchedule boilerSchedule) {
        schedules.put(WORKING, prepareStructure(boilerSchedule.getWorkingDay()));
        schedules.put(WEEKEND, prepareStructure(boilerSchedule.getWeekendDay()));

    }

    @Data
    private static class Point {
        private Integer minute;
        private PointType pointType;

        Point(LocalTime minute) {
            this.minute = minute.get(ChronoField.MINUTE_OF_DAY);

        }

        public Point(LocalTime minute, PointType pointType) {
            this(minute);
            this.pointType = pointType;
        }
    }

    public List<Point> prepareStructure(List<BoilerSchedule.Range> ranges) {
        return ranges.stream().flatMap(r -> Arrays.asList(
                    new Point(r.getTimeFrom(), PointType.FROM),
                    new Point(r.getTimeTo(), PointType.TO))
                .stream())
            .sorted(POINT_COMPARATOR)
            .collect(Collectors.toList());
    }

    public boolean isInRangeNow() {
        final LocalDateTime now = LocalDateTime.now();
        Object key = weekendDaysList.contains(now.getDayOfWeek().getValue()) ? WEEKEND : WORKING;
        return isInRange(now.toLocalTime(), schedules.get(key));
    }


    private boolean isInRange(LocalTime time, List<Point> points) {
        final Location location = getLocation(Collections.binarySearch(points, new Point(time), POINT_COMPARATOR), points);
        return location == Location.HIT || location == Location.FROM_TO;
    }

    private Location getLocation(int i, List<Point> points) {
        if (i >= 0) {
            return Location.HIT;
        } else {
            i = ~i;
            if (i == 0) {
                return Location.START_FROM;
            } else if (i == points.size()) {
                return Location.TO_END;
            } else if (points.get(i).pointType == PointType.TO) {
                return Location.FROM_TO;
            } else {
                return Location.TO_FROM;
            }
        }
    }

    private enum Location {
        START_FROM, FROM_TO, TO_FROM, TO_END, HIT
    }

    private enum PointType {
        FROM, TO;
    }

    /*public static void main(String[] args) {
        final int[] ints = {1, 3, 5};
        //System.out.println(~(-1));
        System.out.println(~Arrays.binarySearch(ints, 2));
        //System.out.println(ints[-(Arrays.binarySearch(ints, 0) + 2)]);
    }*/
}
