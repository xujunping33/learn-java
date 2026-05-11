import java.util.Scanner;

public class SecondsToHMS {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入秒数：");
        int total = scanner.nextInt();

        int hours = total / 3600;
        int minutes = (total % 3600) / 60;
        int seconds = total % 60;

        System.out.println(hours + "小时" + minutes + "分" + seconds + "秒");
        scanner.close();
    }
}

