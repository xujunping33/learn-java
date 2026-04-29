public class ShortCircuitNullDemo {
    public static void main(String[] args) {
        // 用运行时输入决定 s，避免 IDE 认为分支“恒定/死代码”
        // 用法示例：
        //   java -cp day3 ShortCircuitNullDemo            -> s 为 null
        //   java -cp day3 ShortCircuitNullDemo ""         -> s 为空串
        //   java -cp day3 ShortCircuitNullDemo hello      -> s 为 "hello"
        String s = args.length == 0 ? null : args[0];

        if (s != null && s.length() > 0) {
            System.out.println("非空且长度>0");
        } else {
            System.out.println("s 为 null 或为空串（不会触发空指针）");
        }

        String t = "hi";
        if (t != null && t.length() > 0) {
            System.out.println("t OK，长度=" + t.length());
        }
    }
}

