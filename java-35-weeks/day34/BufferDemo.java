public class BufferDemo {
    public static void main(String[] args) {
        int n = 5000;

        long t1 = System.nanoTime();
        String s1 = concatWithString(n);
        long t2 = System.nanoTime();

        long t3 = System.nanoTime();
        String s2 = concatWithStringBuffer(n);
        long t4 = System.nanoTime();

        System.out.println("String 结果长度 = " + s1.length()
                + ", 耗时(ms) = " + ((t2 - t1) / 1_000_000.0));
        System.out.println("StringBuffer 结果长度 = " + s2.length()
                + ", 耗时(ms) = " + ((t4 - t3) / 1_000_000.0));

        System.out.println();
        System.out.println("== 星号图形（每行先 StringBuffer 拼好再输出）==");
        printTriangleWithBuffer(5);
    }

    private static String concatWithString(int n) {
        String out = "";
        for (int i = 1; i <= n; i++) {
            out += i + ",";
        }
        return out;
    }

    private static String concatWithStringBuffer(int n) {
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i <= n; i++) {
            sb.append(i).append(",");
        }
        return sb.toString();
    }

    private static void printTriangleWithBuffer(int rows) {
        for (int i = 1; i <= rows; i++) {
            StringBuffer line = new StringBuffer();
            for (int j = 1; j <= i; j++) {
                line.append("*");
            }
            System.out.println(line);
        }
    }
}

