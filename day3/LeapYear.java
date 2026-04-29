import java.util.Scanner;

public class LeapYear {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入年份：");
        int year = scanner.nextInt();

        boolean leap = (year % 400 == 0) || (year % 4 == 0 && year % 100 != 0);
        System.out.println(leap ? "闰年" : "平年");
        scanner.close();
    }
}

