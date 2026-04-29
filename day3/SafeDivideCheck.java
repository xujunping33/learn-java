import java.util.Scanner;

public class SafeDivideCheck {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入两个整数 a b（计算 a/b）：");
        int a = scanner.nextInt();
        int b = scanner.nextInt();

        if (b != 0 && (a % b == 0)) {
            System.out.println("能整除，结果 = " + (a / b));
        } else if (b == 0) {
            System.out.println("b 为 0，不能做除法");
        } else {
            System.out.println("不能整除，小数结果 = " + (a / (double) b));
        }

        scanner.close();
    }
}

