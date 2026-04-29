import java.util.Scanner;

public class LeapYearIf {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入年份：");

        if (!scanner.hasNextInt()) {
            System.out.println("非法输入：请输入整数年份");
            scanner.close();
            return;
        }

        int year = scanner.nextInt();
        boolean leap;

        if (year % 400 == 0) leap = true;
        else if (year % 100 == 0) leap = false;
        else leap = (year % 4 == 0);

        System.out.println(leap ? "闰年" : "平年");
        scanner.close();
    }
}

