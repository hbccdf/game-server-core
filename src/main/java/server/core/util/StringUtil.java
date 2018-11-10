package server.core.util;

public class StringUtil {
    public static boolean isMatch(String s, String p) {
        boolean[][] dp = new boolean[s.length() + 1][p.length() + 1];
        dp[0][0] = true;
        for (int j = 0; j < p.length(); j++) {
            if (dp[0][j] && p.charAt(j) == '*') {
                dp[0][j + 1] = true;
            }
        }
        for (int i = 0; i < s.length(); i++) {
            for (int j = 0; j < p.length(); j++) {
                if (p.charAt(j) == '*') {
                    dp[i + 1][j + 1] = dp[i][j + 1] || dp[i + 1][j];
                } else if (p.charAt(j) == '?' || p.charAt(j) == s.charAt(i)) {
                    dp[i + 1][j + 1] = dp[i][j];
                }
            }
        }
        return dp[s.length()][p.length()];
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
