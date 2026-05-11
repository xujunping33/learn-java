package learn.java.maven;

public class StringUtils {
    public static String reverse(String s) {
        if (s == null) throw new IllegalArgumentException("s must not be null");
        return new StringBuilder(s).reverse().toString();
    }

    public static int clampScore(int score) {
        if (score < 0) return 0;
        if (score > 100) return 100;
        return score;
    }
}

