import java.util.Scanner;

public class BMI {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入身高（米）：");
        double height = scanner.nextDouble();
        System.out.print("输入体重（公斤）：");
        double weight = scanner.nextDouble();

        double bmi = weight / (height * height);
        System.out.println("BMI = " + bmi);
        scanner.close();
    }
}

