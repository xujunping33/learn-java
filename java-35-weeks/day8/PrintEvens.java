import java.util.Scanner;

public class PrintEvens {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        int n = in.readIntInRange("输入 n（1-100000）：", 1, 100000);
        for (int i = 1; i <= n; i++) {
            if (i % 2 == 0) {
                System.out.print(i);
                if (i + 2 <= n) System.out.print(" ");
            }
        }
        System.out.println();
        scanner.close();
    }
}

