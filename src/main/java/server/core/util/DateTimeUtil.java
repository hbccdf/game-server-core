package server.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateTimeUtil {
    public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static Date parse(String str, String patten) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(patten);
        return sdf.parse(str);
    }

    public static Date parse(String str) throws ParseException {
        return parse(str, DEFAULT_PATTERN);
    }

    public static String format(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static String format(Date date) {
        return format(date, DEFAULT_PATTERN);
    }

    public static String format(long millisecond, String pattern) {
        return format(new Date(millisecond), pattern);

    }

    public static String format(long millisecond) {
        return format(new Date(millisecond));
    }

    public static boolean equalsByDayOfYear(Date d1, Date d2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);

        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static int getInterval(Date time1, Date time2) {
        return (int)(time1.getTime() - time2.getTime());
    }

    public static int getInterval(Date time) {
        return (int) (System.currentTimeMillis() - time.getTime());
    }

    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        //minus number would decrement the days
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    public static long getMilliSecondOfDay(Date date) {
        return getMilliSecondOfDay(date.getTime());
    }

    public static long getMilliSecondOfDay(long millisecond) {
        return millisecond % TimeUtil.ONE_DAY;
    }

    public static long getDays(Date date) {
        return getDays(date.getTime());
    }

    public static long getDays(long millisecond) {
        return millisecond / TimeUtil.ONE_DAY;
    }
}

