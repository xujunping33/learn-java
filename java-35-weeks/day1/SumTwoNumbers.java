import java.util.Scanner;

public class SumTwoNumbers {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("请输入第一个整数：");
        int a = scanner.nextInt();

        System.out.print("请输入第二个整数：");
        int b = scanner.nextInt();

        int sum = a + b;
        System.out.println(a + " + " + b + " = " + sum);

        scanner.close();
    }
}

