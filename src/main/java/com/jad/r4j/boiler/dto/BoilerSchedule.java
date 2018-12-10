package com.jad.r4j.boiler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.split;

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

@Data
public class BoilerSchedule {

    private List<Range> weekendDay;
    private List<Range> workingDay;


    @AllArgsConstructor
    @Data
    public static class Range {
        private LocalTime timeFrom;
        private LocalTime timeTo;
    }

    public static BoilerSchedule parse(String working, String weekend) {
        final BoilerSchedule boilerSchedule = new BoilerSchedule();
        boilerSchedule.workingDay = parse(working);
        boilerSchedule.weekendDay = parse(weekend);
        return boilerSchedule;
    }

    private static List<Range> parse(String string) {
        final ArrayList<Range> ranges = new ArrayList<>();
        for (String range : split(string, ';')) {
            final String[] split = split(range, '-');
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
            ranges.add(new Range(start, end));
        }
        return ranges;
    }

    public static void main(String[] args) {
        System.out.println(split("-234:543", '-').length);
    }

}
