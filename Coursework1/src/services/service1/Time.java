package services.service1;

import services.common.CommonConstants;
import java.util.Random;

public class Time {
    private Integer time;

    public Time()
    {
        this.time = 0;
    }

    public Time(Integer minutes)
    {
        this.time = minutes;
    }

    public Time(Integer days, Integer hours, Integer minutes)
    {
        this.time = days * CommonConstants.HOURS_IN_DAY * CommonConstants.MINUTES_IN_HOUR + hours
                * CommonConstants.MINUTES_IN_HOUR + minutes;
    }

    public void addTime(Integer minutes)
    {
        this.time += minutes;
    }

    public Integer getTime() {
        return time;
    }

    @Override
    public String toString() {
        int days = time / (CommonConstants.HOURS_IN_DAY * CommonConstants.MINUTES_IN_HOUR);
        int hours = (time - days * CommonConstants.HOURS_IN_DAY * CommonConstants.MINUTES_IN_HOUR)
                / CommonConstants.MINUTES_IN_HOUR;
        int minutes = time - (days * CommonConstants.HOURS_IN_DAY * CommonConstants.MINUTES_IN_HOUR
                + hours * CommonConstants.MINUTES_IN_HOUR);

        String result = "";

        if (days < 10 && days > -1) result += '0';
        result += (days + ":");

        if (hours < 10 && hours > -1) result += '0';
        result += (hours + ":");

        if (minutes < 10 && minutes > -1) result += '0';
        result += (minutes);

        return result;
    }

    private static int generateRandomDay(int maxDays)
    {
        return new Random().nextInt(maxDays);
    }

    private static int generateRandomHour(int maxHours)
    {
        return new Random().nextInt(maxHours);
    }

    private static int generateRandomMinutes(int maxMinutes)
    {
        return new Random().nextInt(maxMinutes);
    }

    public static Time getRandomTime(int maxDays, int maxHours, int maxMinutes)
    {
        if (maxHours > CommonConstants.HOURS_IN_DAY)
            throw new IllegalArgumentException("Incorrect amount of hours");

        if (maxMinutes > CommonConstants.MINUTES_IN_HOUR)
            throw new IllegalArgumentException("Incorrect amount of minutes");

        return new Time(generateRandomDay(maxDays), generateRandomHour(maxHours),
                generateRandomMinutes(maxMinutes));
    }

}

