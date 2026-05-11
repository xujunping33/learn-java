import java.util.Scanner;

public class GradeLevelIf {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入分数（0-100）：");

        if (!scanner.hasNextInt()) {
            System.out.println("非法输入：请输入整数分数");
            scanner.close();
            return;
        }

        int score = scanner.nextInt();
        if (score < 0 || score > 100) {
            System.out.println("非法分数：范围应为 0-100");
            scanner.close();
            return;
        }

        String level;
        if (score >= 90) level = "A";
        else if (score >= 80) level = "B";
        else if (score >= 70) level = "C";
        else if (score >= 60) level = "D";
        else level = "E";

        System.out.println("等级 = " + level);
        scanner.close();
    }
}

