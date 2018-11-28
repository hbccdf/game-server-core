package server.core.util;

public class StringUtil {
    public static boolean isMatch(String s, String p) {
        return s.matches(p);
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isNullOrWhiteSpace(String string) {
        return string == null || string.isEmpty() || string.trim().isEmpty();
    }

    /**
     * 将单词的第一个字母大写
     * @param word
     * @return
     */
    public static String firstLetterToUpperCase(String word) {
        StringBuilder sb = new StringBuilder(word);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    /**
     * 将单词的第一个字母小写
     * @param word
     * @return
     */
    public static String firstLetterToLowerCase(String word) {
        StringBuilder sb = new StringBuilder(word);
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }
}
