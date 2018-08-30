package server.core.util;

public class ParseUtil {
    public static int parseInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {

        }
        return defaultValue;
    }
}
