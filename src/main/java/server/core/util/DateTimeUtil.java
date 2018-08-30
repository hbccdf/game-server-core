package server.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

    public static boolean equalsByDayOfYear(Date d1, Date d2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);

        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}

