import java.util.Scanner;

public class FactorialLong {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        long n = in.readLong("输入 n（0-20）：");
        if (n < 0 || n > 20) {
            System.out.println("为避免 long 溢出，这里限制 n 在 0..20");
            scanner.close();
            return;
        }

        long result = 1;
        for (long i = 2; i <= n; i++) {
            result *= i;
        }
        System.out.println(n + "! = " + result);
        scanner.close();
    }
}

