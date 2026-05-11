import java.util.Scanner;

public class LoopMenuDemo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        while (true) {
            System.out.println();
            System.out.println("=== LoopMenuDemo ===");
            System.out.println("1. while vs do-while 小演示");
            System.out.println("2. for 边界：i<n vs i<=n");
            System.out.println("0. 退出");

            int choice = in.readIntInRange("请选择：", 0, 2);
            switch (choice) {
                case 0 -> {
                    System.out.println("Bye.");
                    scanner.close();
                    return;
                }
                case 1 -> demoWhileVsDoWhile(in);
                case 2 -> demoForBoundary(in);
            }
        }
    }

    private static void demoWhileVsDoWhile(SafeInput in) {
        int n = in.readIntInRange("输入 n（0-5）：", 0, 5);

        System.out.println("-- while（先判断）--");
        int i = 0;
        while (i < n) {
            System.out.print(i + " ");
            i++;
        }
        System.out.println();

        System.out.println("-- do-while（先执行一次）--");
        int j = 0;
        do {
            System.out.print(j + " ");
            j++;
        } while (j < n);
        System.out.println();

        System.out.println("当 n=0 时：while 输出为空；do-while 仍会输出 0（因为先执行一次）。");
    }

    private static void demoForBoundary(SafeInput in) {
        int n = in.readIntInRange("输入 n（0-10）：", 0, 10);
        System.out.println("-- for i < n --");
        for (int i = 0; i < n; i++) {
            System.out.print(i + " ");
        }
        System.out.println();

        System.out.println("-- for i <= n --");
        for (int i = 0; i <= n; i++) {
            System.out.print(i + " ");
        }
        System.out.println();
    }
}

