import java.util.Scanner;

public class SimpleInterest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入本金 P：");
        double p = scanner.nextDouble();
        System.out.print("输入年利率（例如 0.035）：");
        double r = scanner.nextDouble();
        System.out.print("输入年数 t：");
        double t = scanner.nextDouble();

        double interest = p * r * t;
        double total = p + interest;
        System.out.println("利息 = " + interest);
        System.out.println("本息合计 = " + total);
        scanner.close();
    }
}

