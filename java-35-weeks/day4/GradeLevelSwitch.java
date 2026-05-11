import java.util.Scanner;

public class GradeLevelSwitch {
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

        int bucket = score / 10; // 0..10
        String level;
        switch (bucket) {
            case 10, 9 -> level = "A"; // 90-100
            case 8 -> level = "B";
            case 7 -> level = "C";
            case 6 -> level = "D";
            default -> level = "E"; // 0-59
        }

        System.out.println("等级 = " + level);
        scanner.close();
    }
}

