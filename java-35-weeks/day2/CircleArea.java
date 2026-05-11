import java.util.Scanner;

public class CircleArea {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入半径 r：");
        double r = scanner.nextDouble();
        final double PI = Math.PI;
        double area = PI * r * r;
        System.out.println("圆面积 = " + area);
        scanner.close();
    }
}

