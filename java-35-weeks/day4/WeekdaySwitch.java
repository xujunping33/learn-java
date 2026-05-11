import java.util.Scanner;

public class WeekdaySwitch {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入数字 1-7：");

        if (!scanner.hasNextInt()) {
            System.out.println("非法输入：请输入 1-7 的整数");
            scanner.close();
            return;
        }

        int n = scanner.nextInt();
        String day;

        switch (n) {
            case 1 -> day = "星期一";
            case 2 -> day = "星期二";
            case 3 -> day = "星期三";
            case 4 -> day = "星期四";
            case 5 -> day = "星期五";
            case 6 -> day = "星期六";
            case 7 -> day = "星期日";
            default -> {
                System.out.println("非法输入：范围应为 1-7");
                scanner.close();
                return;
            }
        }

        System.out.println(day);
        scanner.close();
    }
}

