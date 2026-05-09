import java.util.ArrayList;
import java.util.Scanner;

public class SafeCalculatorV2 {
    private static final int HISTORY_LIMIT = 20;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> history = new ArrayList<>();

        while (true) {
            printMenu();
            try {
                int choice = readMenuChoice(scanner);
                switch (choice) {
                    case 0 -> {
                        System.out.println("退出。");
                        scanner.close();
                        return;
                    }
                    case 5 -> printHistory(history);
                    case 1, 2, 3, 4 -> {
                        double a = readDouble(scanner, "输入第一个数：");
                        double b = readDouble(scanner, "输入第二个数：");
                        String record = doCalc(choice, a, b);
                        System.out.println(record);
                        addHistory(history, record);
                    }
                    default -> throw new IllegalArgumentException("菜单越界，请输入 0-5。");
                }
            } catch (Exception e) {
                System.out.println("错误：" + e.getMessage());
                // 继续下一轮，不崩溃退出
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== SafeCalculatorV2 ===");
        System.out.println("1. 加");
        System.out.println("2. 减");
        System.out.println("3. 乘");
        System.out.println("4. 除");
        System.out.println("5. 历史");
        System.out.println("0. 退出");
    }

    private static int readMenuChoice(Scanner scanner) {
        String line = readLine(scanner, "请选择：");
        int v = parseInt(line, "菜单项必须是整数");
        if (v < 0 || v > 5) throw new IllegalArgumentException("菜单越界，请输入 0-5。");
        return v;
    }

    private static double readDouble(Scanner scanner, String prompt) {
        String line = readLine(scanner, prompt);
        try {
            return Double.parseDouble(line.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("请输入合法数字：" + line, e);
        }
    }

    private static String doCalc(int op, double a, double b) {
        return switch (op) {
            case 1 -> fmt(a) + " + " + fmt(b) + " = " + fmt(a + b);
            case 2 -> fmt(a) + " - " + fmt(b) + " = " + fmt(a - b);
            case 3 -> fmt(a) + " * " + fmt(b) + " = " + fmt(a * b);
            case 4 -> {
                if (b == 0.0) {
                    throw new ArithmeticException("除数不能为 0");
                }
                yield fmt(a) + " / " + fmt(b) + " = " + fmt(a / b);
            }
            default -> throw new IllegalArgumentException("未知操作");
        };
    }

    private static void addHistory(ArrayList<String> history, String record) {
        history.add(record);
        if (history.size() > HISTORY_LIMIT) history.remove(0);
    }

    private static void printHistory(ArrayList<String> history) {
        System.out.println();
        System.out.println("=== 历史记录（最多 " + HISTORY_LIMIT + " 条）===");
        if (history.isEmpty()) {
            System.out.println("<empty>");
            return;
        }
        for (int i = 0; i < history.size(); i++) {
            System.out.println((i + 1) + ". " + history.get(i));
        }
    }

    private static String readLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new IllegalStateException("输入流已关闭");
        }
        return scanner.nextLine();
    }

    private static int parseInt(String s, String errMsg) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errMsg + " -> " + s, e);
        }
    }

    private static String fmt(double x) {
        if (x == (long) x) return String.valueOf((long) x);
        return String.format("%.6f", x).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}

