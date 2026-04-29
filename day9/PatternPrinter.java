import java.util.Scanner;

public class PatternPrinter {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        while (true) {
            System.out.println();
            System.out.println("=== PatternPrinter ===");
            System.out.println("1. 实心直角三角形");
            System.out.println("2. 等腰三角形（金字塔）");
            System.out.println("3. 空心矩形");
            System.out.println("0. 退出");

            int choice = in.readIntInRange("请选择：", 0, 3);
            switch (choice) {
                case 0 -> {
                    System.out.println("Bye.");
                    scanner.close();
                    return;
                }
                case 1 -> {
                    int h = in.readIntInRange("高度（1-20）：", 1, 20);
                    rightTriangle(h);
                }
                case 2 -> {
                    int h = in.readIntInRange("高度（1-20）：", 1, 20);
                    pyramid(h);
                }
                case 3 -> {
                    int rows = in.readIntInRange("行数（1-20）：", 1, 20);
                    int cols = in.readIntInRange("列数（1-40）：", 1, 40);
                    hollowRectangle(rows, cols);
                }
            }
        }
    }

    private static void rightTriangle(int h) {
        for (int i = 1; i <= h; i++) {
            for (int j = 1; j <= i; j++) {
                System.out.print("*");
            }
            System.out.println();
        }
    }

    private static void pyramid(int h) {
        for (int i = 1; i <= h; i++) {
            int spaces = h - i;
            int stars = 2 * i - 1;

            for (int s = 0; s < spaces; s++) System.out.print(" ");
            for (int k = 0; k < stars; k++) System.out.print("*");
            System.out.println();
        }
    }

    private static void hollowRectangle(int rows, int cols) {
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                boolean border = (i == 1 || i == rows || j == 1 || j == cols);
                System.out.print(border ? "*" : " ");
            }
            System.out.println();
        }
    }
}

