import java.util.Scanner;

public class MinMaxTenInputs {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        long min = 0;
        long max = 0;

        for (int i = 1; i <= 10; i++) {
            long x = in.readLong("第 " + i + " 次输入整数：");
            if (i == 1) {
                min = x;
                max = x;
            } else {
                if (x < min) min = x;
                if (x > max) max = x;
            }
        }

        System.out.println("min = " + min);
        System.out.println("max = " + max);
        scanner.close();
    }
}

