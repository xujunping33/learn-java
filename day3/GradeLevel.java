import java.util.Scanner;

public class GradeLevel {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入分数（0-100）：");
        int score = scanner.nextInt();

        if (score < 0 || score > 100) {
            System.out.println("非法分数");
            scanner.close();
            return;
        }

        String level =
                score >= 90 ? "A" :
                score >= 80 ? "B" :
                score >= 70 ? "C" :
                score >= 60 ? "D" : "E";

        System.out.println("等级 = " + level);
        scanner.close();
    }
}

