import java.util.Scanner;

public class Sum1ToNFor {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        long n = in.readLong("输入 n（>=1）：");
        if (n < 1) {
            System.out.println("n 必须 >= 1");
            scanner.close();
            return;
        }

        long sum = 0;
        for (long i = 1; i <= n; i++) {
            sum += i;
        }
        System.out.println("sum(1.." + n + ") = " + sum);
        scanner.close();
    }
}

