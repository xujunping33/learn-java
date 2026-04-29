import java.util.Scanner;

public class AverageThree {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入三个数（用空格分隔）：");
        double a = scanner.nextDouble();
        double b = scanner.nextDouble();
        double c = scanner.nextDouble();

        double avg = (a + b + c) / 3.0;
        System.out.println("平均值 = " + avg);
        scanner.close();
    }
}

