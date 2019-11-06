package com.jad.r4j.boiler.dto;

import java.beans.ConstructorProperties;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class BoilerSchedule {
   private List<BoilerSchedule.Range> weekendDay;
   private List<BoilerSchedule.Range> workingDay;

   public static BoilerSchedule parse(String working, String weekend) {
      BoilerSchedule boilerSchedule = new BoilerSchedule();
      boilerSchedule.workingDay = parse(working);
      boilerSchedule.weekendDay = parse(weekend);
      return boilerSchedule;
   }

   private static List<BoilerSchedule.Range> parse(String string) {
      ArrayList<BoilerSchedule.Range> ranges = new ArrayList();
      String[] var2 = StringUtils.split(string, ';');
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String range = var2[var4];
         String[] split = StringUtils.split(range, '-');
         LocalTime start;
         LocalTime end;
         if (split.length == 1) {
            if (StringUtils.startsWith(range, "-")) {
               start = LocalTime.MIN;
               end = LocalTime.parse(split[0]);
            } else {
               start = LocalTime.parse(split[0]);
               end = LocalTime.MAX;
            }
         } else {
            start = LocalTime.parse(split[0]);
            end = LocalTime.parse(split[1]);
         }

         ranges.add(new BoilerSchedule.Range(start, end));
      }

      return ranges;
   }

   public static void main(String[] args) {
      System.out.println(StringUtils.split("-234:543", '-').length);
   }



   @Data
   public static class Range {
      private LocalTime timeFrom;
      private LocalTime timeTo;

      @ConstructorProperties({"timeFrom", "timeTo"})
      public Range(LocalTime timeFrom, LocalTime timeTo) {
         this.timeFrom = timeFrom;
         this.timeTo = timeTo;
      }

   }
}
