import java.util.Scanner;

public class CelsiusToFahrenheit {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入摄氏温度 C：");
        double c = scanner.nextDouble();
        double f = c * 9 / 5 + 32;
        System.out.println("华氏温度 F = " + f);
        scanner.close();
    }
}

