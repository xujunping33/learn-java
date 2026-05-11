import java.util.Scanner;

public class CompareTwoNumbers {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入两个整数 a b：");
        int a = scanner.nextInt();
        int b = scanner.nextInt();

        System.out.println("a == b ? " + (a == b));
        System.out.println("a != b ? " + (a != b));
        System.out.println("a >  b ? " + (a > b));
        System.out.println("a >= b ? " + (a >= b));
        System.out.println("a <  b ? " + (a < b));
        System.out.println("a <= b ? " + (a <= b));

        scanner.close();
    }
}

