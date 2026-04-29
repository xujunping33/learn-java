import java.util.Scanner;

public class MaxOfTwo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入两个整数 a b：");
        int a = scanner.nextInt();
        int b = scanner.nextInt();
        int max = a > b ? a : b;
        System.out.println("最大值 = " + max);
        scanner.close();
    }
}

