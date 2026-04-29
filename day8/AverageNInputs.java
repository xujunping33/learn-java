import java.util.Scanner;

public class AverageNInputs {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        int n = in.readIntInRange("输入 n（1-100000）：", 1, 100000);
        double sum = 0.0;

        for (int i = 1; i <= n; i++) {
            double x = in.readDouble("第 " + i + " 个数：");
            sum += x;
        }

        double avg = sum / n;
        System.out.println("平均值 = " + avg);
        scanner.close();
    }
}

