public class TypePractice {
    public static void main(String[] args) {
        int a = 1;
        int b = 2;

        System.out.println("1) int 除法：1/2 = " + (a / b));
        System.out.println("2) double 除法：1/2.0 = " + (a / 2.0));
        System.out.println("3) 强制类型转换：(double)1/2 = " + ((double) a / b));

        double pi = 3.14159;
        int piInt = (int) pi;
        System.out.println("4) 强转会丢小数：(int)3.14159 = " + piInt);

        char ch = 'A';
        int chCode = ch;
        System.out.println("5) char -> int（ASCII/Unicode 编码）：'A' = " + chCode);

        int big = 130;
        byte small = (byte) big;
        System.out.println("6) int -> byte 可能溢出：130 -> " + small);

        final int DAYS_IN_WEEK = 7;
        System.out.println("7) final 常量示例：DAYS_IN_WEEK = " + DAYS_IN_WEEK);
    }
}

