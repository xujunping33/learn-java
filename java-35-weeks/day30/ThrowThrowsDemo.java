import java.io.BufferedReader;
import java.io.FileReader;

public class ThrowThrowsDemo {
    public static void main(String[] args) {
        System.out.println("== 1) throw：方法内部主动抛出 ==");
        testParse("42");
        testParse("-7");
        testParse("abc");

        System.out.println();
        System.out.println("== 2) throws：方法签名声明，调用处决定如何处理 ==");
        testReadFile("day30/sample.txt");
        testReadFile("day30/not-exist.txt");
    }

    private static void testParse(String s) {
        try {
            int v = parsePositiveInt(s);
            System.out.println("parsePositiveInt(\"" + s + "\") = " + v);
        } catch (IllegalArgumentException e) {
            System.out.println("输入 \"" + s + "\" 非法：" + e.getMessage());
        }
    }

    // throw 示例：在方法内部主动抛异常
    public static int parsePositiveInt(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("输入不能为空");
        }
        int value;
        try {
            value = Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("不是合法整数：" + s, e);
        }
        if (value <= 0) {
            throw new IllegalArgumentException("必须是正整数，实际为：" + value);
        }
        return value;
    }

    private static void testReadFile(String path) {
        try {
            String line = readFileFirstLine(path);
            System.out.println(path + " 第一行：" + line);
        } catch (Exception e) {
            System.out.println("读取文件失败（" + path + "）：" + e.getMessage());
        }
    }

    // throws 示例：把异常处理责任交给调用者
    public static String readFileFirstLine(String path) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            return line == null ? "" : line;
        }
    }
}

