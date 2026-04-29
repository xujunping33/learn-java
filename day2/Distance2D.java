import java.util.Scanner;

public class Distance2D {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入点1坐标 x1 y1：");
        double x1 = scanner.nextDouble();
        double y1 = scanner.nextDouble();
        System.out.print("输入点2坐标 x2 y2：");
        double x2 = scanner.nextDouble();
        double y2 = scanner.nextDouble();

        double dx = x2 - x1;
        double dy = y2 - y1;
        double d = Math.sqrt(dx * dx + dy * dy);

        System.out.println("距离 = " + d);
        scanner.close();
    }
}

