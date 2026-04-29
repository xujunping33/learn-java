import java.util.Scanner;

public class LeapYearSwitch {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入年份：");

        if (!scanner.hasNextInt()) {
            System.out.println("非法输入：请输入整数年份");
            scanner.close();
            return;
        }

        int year = scanner.nextInt();

        if (year % 400 == 0) {
            System.out.println("闰年");
            scanner.close();
            return;
        }

        if (year % 100 == 0) {
            System.out.println("平年");
            scanner.close();
            return;
        }

        switch (year % 4) {
            case 0 -> System.out.println("闰年");
            default -> System.out.println("平年");
        }

        scanner.close();
    }
}

