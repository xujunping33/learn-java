import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static final int HISTORY_LIMIT = 20;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);
        Calculator calc = new Calculator();
        ArrayList<String> history = new ArrayList<>();

        while (true) {
            printMenu();
            int choice = in.readIntInRange("请选择：", 0, 5);

            switch (choice) {
                case 0 -> {
                    System.out.println("退出。");
                    scanner.close();
                    return;
                }
                case 5 -> printHistory(history);
                case 1, 2, 3, 4 -> {
                    double a = in.readDouble("输入第一个数：");
                    double b = in.readDouble("输入第二个数：");
                    handleOperation(choice, a, b, calc, history);
                }
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== MenuCalculator ===");
        System.out.println("1. 加");
        System.out.println("2. 减");
        System.out.println("3. 乘");
        System.out.println("4. 除");
        System.out.println("5. 历史");
        System.out.println("0. 退出");
    }

    private static void handleOperation(
            int op,
            double a,
            double b,
            Calculator calc,
            ArrayList<String> history
    ) {
        String expr;
        Double result;

        switch (op) {
            case 1 -> {
                result = calc.add(a, b);
                expr = formatExpr(a, "+", b, result);
            }
            case 2 -> {
                result = calc.sub(a, b);
                expr = formatExpr(a, "-", b, result);
            }
            case 3 -> {
                result = calc.mul(a, b);
                expr = formatExpr(a, "*", b, result);
            }
            case 4 -> {
                result = calc.div(a, b);
                if (result == null) {
                    System.out.println("错误：除数不能为 0。");
                    expr = formatExpr(a, "/", b, null);
                } else {
                    expr = formatExpr(a, "/", b, result);
                }
            }
            default -> {
                System.out.println("未知操作。");
                return;
            }
        }

        if (result != null) {
            System.out.println(expr);
        }
        addHistory(history, expr);
    }

    private static String formatExpr(double a, String op, double b, Double result) {
        if (result == null) {
            return String.format("%s %s %s = <error>", fmt(a), op, fmt(b));
        }
        return String.format("%s %s %s = %s", fmt(a), op, fmt(b), fmt(result));
    }

    private static String fmt(double x) {
        if (Double.isNaN(x) || Double.isInfinite(x)) return String.valueOf(x);
        if (x == (long) x) return String.format("%d", (long) x);
        return String.format("%.6f", x).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static void addHistory(ArrayList<String> history, String entry) {
        history.add(entry);
        if (history.size() > HISTORY_LIMIT) {
            history.remove(0);
        }
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
}

