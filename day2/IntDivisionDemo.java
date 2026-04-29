import java.util.Scanner;

public class IntDivisionDemo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入两个整数 a b：");
        int a = scanner.nextInt();
        int b = scanner.nextInt();

        System.out.println("a / b（整数除法）= " + (a / b));
        System.out.println("a / b（转成小数）= " + (a / (double) b));
        scanner.close();
    }
}

