import java.util.Scanner;

public class SafeInputDemo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        System.out.println("=== SafeInput Demo ===");
        String name = in.readLine("输入姓名：").trim();
        int age = in.readIntInRange("输入年龄（0-150）：", 0, 150);
        double height = in.readDouble("输入身高（米，例如 1.75）：");

        System.out.println();
        System.out.println("=== 结果 ===");
        System.out.println("name=" + name);
        System.out.println("age=" + age);
        System.out.println("height=" + height);

        System.out.println();
        System.out.println("=== 小练习：菜单（用断点调试）===");
        int choice = in.readIntInRange("输入菜单选项（0-3）：", 0, 3);
        switch (choice) {
            case 0 -> System.out.println("退出");
            case 1 -> System.out.println("你选择了 1");
            case 2 -> System.out.println("你选择了 2");
            case 3 -> System.out.println("你选择了 3");
        }

        scanner.close();
    }
}

