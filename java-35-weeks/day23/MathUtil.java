public final class MathUtil {
    private MathUtil() {
        // 工具类不应该被 new
    }

    public static int clamp(int x, int min, int max) {
        if (min > max) throw new IllegalArgumentException("min > max");
        if (x < min) return min;
        if (x > max) return max;
        return x;
    }

    public static int abs(int x) {
        return x >= 0 ? x : -x;
    }
}

