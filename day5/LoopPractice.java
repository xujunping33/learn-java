import java.util.Scanner;

public class LoopPractice {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println();
            System.out.println("=== Day5 LoopPractice ===");
            System.out.println("1. 1..n 求和");
            System.out.println("2. 阶乘 n!");
            System.out.println("3. 统计正数/负数个数（输入 0 结束）");
            System.out.println("4. 打印 9x9 乘法表");
            System.out.println("5. 判断质数");
            System.out.println("6. 最大公约数/最小公倍数");
            System.out.println("0. 退出");
            System.out.print("请选择：");

            if (!scanner.hasNextInt()) {
                System.out.println("请输入菜单数字。");
                scanner.next();
                continue;
            }
            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> sum1ToN(scanner);
                case 2 -> factorial(scanner);
                case 3 -> countPosNeg(scanner);
                case 4 -> multiplicationTable();
                case 5 -> primeCheck(scanner);
                case 6 -> gcdLcm(scanner);
                case 0 -> {
                    System.out.println("Bye.");
                    scanner.close();
                    return;
                }
                default -> System.out.println("无效选项，请重试。");
            }
        }
    }

    private static void sum1ToN(Scanner scanner) {
        System.out.print("输入 n（>=1）：");
        long n = readLong(scanner);
        if (n < 1) {
            System.out.println("n 必须 >= 1");
            return;
        }
        long sum = 0;
        for (long i = 1; i <= n; i++) {
            sum += i;
        }
        System.out.println("sum(1.." + n + ") = " + sum);
    }

    private static void factorial(Scanner scanner) {
        System.out.print("输入 n（0-20）：");
        long n = readLong(scanner);
        if (n < 0 || n > 20) {
            System.out.println("为避免 long 溢出，这里限制 n 在 0..20");
            return;
        }
        long result = 1;
        long i = 2;
        while (i <= n) {
            result *= i;
            i++;
        }
        System.out.println(n + "! = " + result);
    }

    private static void countPosNeg(Scanner scanner) {
        int pos = 0;
        int neg = 0;
        System.out.println("请输入整数，输入 0 结束：");

        while (true) {
            System.out.print("> ");
            long x = readLong(scanner);
            if (x == 0) break;
            if (x > 0) pos++;
            else neg++;
        }

        System.out.println("正数个数 = " + pos);
        System.out.println("负数个数 = " + neg);
    }

    private static void multiplicationTable() {
        for (int i = 1; i <= 9; i++) {
            for (int j = 1; j <= i; j++) {
                System.out.print(j + "x" + i + "=" + (i * j));
                if (j != i) System.out.print("\t");
            }
            System.out.println();
        }
    }

    private static void primeCheck(Scanner scanner) {
        System.out.print("输入 n（>=2）：");
        long n = readLong(scanner);
        if (n < 2) {
            System.out.println("n 必须 >= 2");
            return;
        }

        boolean prime = true;
        for (long d = 2; d * d <= n; d++) {
            if (n % d == 0) {
                prime = false;
                break;
            }
        }

        System.out.println(prime ? "质数" : "合数");
    }

    private static void gcdLcm(Scanner scanner) {
        System.out.print("输入两个正整数 a b：");
        long a = readLong(scanner);
        long b = readLong(scanner);
        if (a <= 0 || b <= 0) {
            System.out.println("a 和 b 必须为正整数");
            return;
        }

        long gcd = gcd(a, b);
        long lcm = (a / gcd) * b; // 先除后乘，降低溢出风险
        System.out.println("gcd = " + gcd);
        System.out.println("lcm = " + lcm);
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    private static long readLong(Scanner scanner) {
        while (!scanner.hasNextLong()) {
            System.out.print("请输入整数：");
            scanner.next();
        }
        return scanner.nextLong();
    }
}

