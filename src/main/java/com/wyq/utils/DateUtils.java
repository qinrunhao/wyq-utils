package com.wyq.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateUtils {

    public static LocalDate getCurrentWeekStartDate(LocalDate localDate) {
        return  localDate.minusDays(localDate.getDayOfWeek().getValue() - 1);
    }

    public static LocalDate getCurrentWeekEndDate(LocalDate localDate) {
        return getCurrentWeekStartDate(localDate.plusWeeks(1)).minusDays(1);
    }

    public static LocalDate getCurrentMonthStartDate(LocalDate localDate) {
        return localDate.minusDays(localDate.getDayOfMonth() - 1);
    }

    public static LocalDate getCurrentMonthEndDate(LocalDate localDate) {
        return getCurrentMonthStartDate(localDate.plusMonths(1)).minusDays(1);
    }

    public static LocalDate getCurrentQuarterStartDate(LocalDate localDate) {
        int month = localDate.getMonthValue();
        if (month <=3) {
            return getCurrentMonthStartDate(localDate.withMonth(1));
        }
        else if (month <= 6) {
            return getCurrentMonthStartDate(localDate.withMonth(4));
        }
        else if (month <= 9) {
            return getCurrentMonthStartDate(localDate.withMonth(7));
        }
        else {
            return getCurrentMonthStartDate(localDate.withMonth(10));
        }
    }

    public static LocalDate getCurrentQuarterEndDate(LocalDate localDate) {
        int month = localDate.getMonthValue();
        if (month <=3) {
            return getCurrentMonthEndDate(localDate.withMonth(3));
        }
        else if (month <= 6) {
            return getCurrentMonthEndDate(localDate.withMonth(6));
        }
        else if (month <= 9) {
            return getCurrentMonthEndDate(localDate.withMonth(9));
        }
        else {
            return getCurrentMonthEndDate(localDate.withMonth(12));
        }
    }

    public static LocalDate getCurrentYearStartDate(LocalDate localDate) {
        return getCurrentMonthStartDate(localDate.withMonth(1));
    }

    public static LocalDate getCurrentYearEndDate(LocalDate localDate) {
        return getCurrentMonthEndDate(localDate.withMonth(12));
    }

    public static LocalDateTime getCurrentDayStartTime(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.MIN);
    }

    public static LocalDateTime getCurrentDayEndTime(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.MAX);
    }

    public static LocalDateTime getCurrentWeekStartTime(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.minusDays(localDateTime.getDayOfWeek().getValue() - 1).toLocalDate(), LocalTime.MIN);
    }

    public static LocalDateTime getCurrentWeekEndTime(LocalDateTime localDateTime) {
        return getCurrentWeekStartTime(localDateTime.plusWeeks(1)).minusNanos(1);
    }

    public static LocalDateTime getCurrentMonthStartTime(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.minusDays(localDateTime.getDayOfMonth() - 1).toLocalDate(), LocalTime.MIN);
    }

    public static LocalDateTime getCurrentMonthEndTime(LocalDateTime localDateTime) {
        return getCurrentMonthStartTime(localDateTime.plusMonths(1)).minusNanos(1);
    }

    public static LocalDateTime getCurrentQuarterStartTime(LocalDateTime localDateTime) {
        int month = localDateTime.getMonthValue();
        if (month <=3) {
            return getCurrentMonthStartTime(localDateTime.withMonth(1));
        }
        else if (month <= 6) {
            return getCurrentMonthStartTime(localDateTime.withMonth(4));
        }
        else if (month <= 9) {
            return getCurrentMonthStartTime(localDateTime.withMonth(7));
        }
        else {
            return getCurrentMonthStartTime(localDateTime.withMonth(10));
        }
    }

    public static LocalDateTime getCurrentQuarterEndTime(LocalDateTime localDateTime) {
        int month = localDateTime.getMonthValue();
        if (month <=3) {
            return getCurrentMonthEndTime(localDateTime.withMonth(3));
        }
        else if (month <= 6) {
            return getCurrentMonthEndTime(localDateTime.withMonth(6));
        }
        else if (month <= 9) {
            return getCurrentMonthEndTime(localDateTime.withMonth(9));
        }
        else {
            return getCurrentMonthEndTime(localDateTime.withMonth(12));
        }
    }

    public static LocalDateTime getCurrentYearStartTime(LocalDateTime localDateTime) {
        return getCurrentMonthStartTime(localDateTime.withMonth(1));
    }

    public static LocalDateTime getCurrentYearEndTime(LocalDateTime localDateTime) {
        return getCurrentMonthEndTime(localDateTime.withMonth(12));
    }

    public static String localDateTimeToString(LocalDateTime localDateTime,String pattern){
        DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern);
        return df.format(localDateTime);
    }

    public static Long localDateTimeToLong(LocalDateTime localDateTime){
        return localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    public static long localDateToLong(LocalDate localDate) {
        return localDate.atStartOfDay().toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }


    public static LocalDateTime strToLocalDateTime(String dateStr, String pattern){
        DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateStr,df);
    }

    public static LocalDateTime longToLocalDateTime(Long timestamp){
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    public static LocalTime longToLocalTime(Long timestamp) {
        return longToLocalDateTime(timestamp).toLocalTime();
    }

    public static long localTimeToLong(LocalTime localTime) {
        return LocalDateTime.of(LocalDate.now(), localTime).toEpochSecond(ZoneOffset.ofHours(8)) * 1000;
    }

    public static List<LocalDate> getWeekDays(LocalDate localDate) {
        List<LocalDate> list = new ArrayList<>();
        LocalDate start = getCurrentWeekStartDate(localDate);
        LocalDate end = getCurrentWeekEndDate(localDate);
        do {
            list.add(start);
            start = start.plusDays(1);
        } while (start.isBefore(end) || start.isEqual(end));
        return list;
    }

}