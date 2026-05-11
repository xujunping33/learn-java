import java.util.Scanner;

public class InputEcho {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("请输入姓名：");
        String name = scanner.nextLine().trim();

        System.out.print("请输入年龄：");
        int age = scanner.nextInt();

        System.out.println("你好，" + name + "！你的年龄是 " + age + "。");
        scanner.close();
    }
}

