package com.jad.r4j.boiler.impl;

import com.jad.r4j.boiler.dto.BoilerSchedule;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DateProcessor {
   private static final Object WORKING = new Object();
   private static final Object WEEKEND = new Object();
   public static final Comparator<DateProcessor.Point> POINT_COMPARATOR = Comparator.comparing(DateProcessor.Point::getMinute);
   private static final int SATURDAY = 6;
   private static final int SUNDAY = 7;
   private static Set<Integer> weekendDaysList = new HashSet<>(Arrays.asList(6, 7));
   private Map<Object, List<DateProcessor.Point>> schedules = new HashMap<>();

   public DateProcessor(BoilerSchedule boilerSchedule) {
      this.schedules.put(WORKING, this.prepareStructure(boilerSchedule.getWorkingDay()));
      this.schedules.put(WEEKEND, this.prepareStructure(boilerSchedule.getWeekendDay()));
   }

   public List<DateProcessor.Point> prepareStructure(List<BoilerSchedule.Range> ranges) {
      return ranges.stream().flatMap((r) -> {
         return Arrays.asList(new DateProcessor.Point(r.getTimeFrom(), DateProcessor.PointType.FROM), new DateProcessor.Point(r.getTimeTo(), DateProcessor.PointType.TO)).stream();
      }).sorted(POINT_COMPARATOR).collect(Collectors.toList());
   }

   public boolean isInRangeNow() {
      LocalDateTime now = LocalDateTime.now();
      Object key = weekendDaysList.contains(now.getDayOfWeek().getValue()) ? WEEKEND : WORKING;
      return this.isInRange(now.toLocalTime(), this.schedules.get(key));
   }

   private boolean isInRange(LocalTime time, List<DateProcessor.Point> points) {
      DateProcessor.Location location = this.getLocation(Collections.binarySearch(points, new DateProcessor.Point(time), POINT_COMPARATOR), points);
      return location == DateProcessor.Location.HIT || location == DateProcessor.Location.FROM_TO;
   }

   private DateProcessor.Location getLocation(int i, List<DateProcessor.Point> points) {
      if (i >= 0) {
         return DateProcessor.Location.HIT;
      } else {
         i = ~i;
         if (i == 0) {
            return DateProcessor.Location.START_FROM;
         } else if (i == points.size()) {
            return DateProcessor.Location.TO_END;
         } else {
            return ((DateProcessor.Point)points.get(i)).pointType == DateProcessor.PointType.TO ? DateProcessor.Location.FROM_TO : DateProcessor.Location.TO_FROM;
         }
      }
   }

   private static enum PointType {
      FROM,
      TO;

      private PointType() {
      }
   }

   private static enum Location {
      START_FROM,
      FROM_TO,
      TO_FROM,
      TO_END,
      HIT;

      private Location() {
      }
   }

   @Data
   private static class Point {
      private Integer minute;
      private DateProcessor.PointType pointType;

      Point(LocalTime minute) {
         this.minute = minute.get(ChronoField.MINUTE_OF_DAY);
      }

      public Point(LocalTime minute, DateProcessor.PointType pointType) {
         this(minute);
         this.pointType = pointType;
      }

   }
}
