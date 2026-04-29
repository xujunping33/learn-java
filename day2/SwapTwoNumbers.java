import java.util.Scanner;

public class SwapTwoNumbers {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入 a：");
        int a = scanner.nextInt();
        System.out.print("输入 b：");
        int b = scanner.nextInt();

        int tmp = a;
        a = b;
        b = tmp;

        System.out.println("交换后：a=" + a + ", b=" + b);
        scanner.close();
    }
}

