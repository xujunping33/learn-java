public class WrapperDemo {
    public static void main(String[] args) {
        System.out.println("== 1) 自动装箱 / 拆箱 ==");
        Integer boxed = 42;      // 自动装箱：int -> Integer
        int unboxed = boxed;     // 自动拆箱：Integer -> int
        System.out.println("boxed = " + boxed + ", unboxed = " + unboxed);

        System.out.println();
        System.out.println("== 2) Integer 缓存与 == ==");
        Integer a = 127;
        Integer b = 127;
        Integer c = 128;
        Integer d = 128;
        System.out.println("127: a==b ? " + (a == b));
        System.out.println("128: c==d ? " + (c == d));
        System.out.println("128: c.equals(d) ? " + c.equals(d));

        System.out.println();
        System.out.println("== 3) null 拆箱风险 ==");
        Integer n = null;
        try {
            int x = n; // 这里会触发 NullPointerException
            System.out.println("x = " + x);
        } catch (NullPointerException e) {
            System.out.println("捕获到 NPE：null 不能拆箱成基本类型 int");
        }

        System.out.println();
        System.out.println("== 4) safeUnbox(Integer, defaultValue) ==");
        System.out.println("safeUnbox(null, -1) = " + safeUnbox(null, -1));
        System.out.println("safeUnbox(99, -1) = " + safeUnbox(99, -1));
    }

    public static int safeUnbox(Integer x, int defaultValue) {
        return x == null ? defaultValue : x;
    }
}

