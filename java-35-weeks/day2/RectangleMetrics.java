import java.util.Scanner;

public class RectangleMetrics {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入长 length：");
        double length = scanner.nextDouble();
        System.out.print("输入宽 width：");
        double width = scanner.nextDouble();

        double perimeter = 2 * (length + width);
        double area = length * width;
        System.out.println("周长 = " + perimeter);
        System.out.println("面积 = " + area);
        scanner.close();
    }
}

