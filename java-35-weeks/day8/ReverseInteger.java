import java.util.Scanner;

public class ReverseInteger {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        long n = in.readLong("输入一个整数（例如 123 或 -120）：");
        boolean neg = n < 0;
        long x = Math.abs(n);

        long rev = 0;
        if (x == 0) {
            rev = 0;
        } else {
            while (x > 0) {
                rev = rev * 10 + (x % 10);
                x /= 10;
            }
        }

        if (neg) rev = -rev;
        System.out.println("反向结果 = " + rev);
        scanner.close();
    }
}

