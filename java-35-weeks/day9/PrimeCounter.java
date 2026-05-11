import java.util.Scanner;

public class PrimeCounter {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        int n = in.readIntInRange("统计 1..n 的质数个数，输入 n（2-200000）：", 2, 200000);

        int count = 0;
        for (int x = 2; x <= n; x++) {
            if (isPrime(x)) count++;
        }

        System.out.println("质数个数 = " + count);
        scanner.close();
    }

    private static boolean isPrime(int x) {
        if (x < 2) return false;
        if (x == 2) return true;
        if (x % 2 == 0) return false;
        for (int d = 3; d * d <= x; d += 2) {
            if (x % d == 0) return false;
        }
        return true;
    }
}

