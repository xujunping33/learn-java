import java.util.Scanner;

public class EvenOdd {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入一个整数：");
        int n = scanner.nextInt();
        System.out.println(n % 2 == 0 ? "偶数" : "奇数");
        scanner.close();
    }
}

