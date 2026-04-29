import java.util.Scanner;

public class SumTwoNumbersSafe {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== SumTwoNumbersSafe ===");
        int a = readInt(scanner, "请输入第一个整数：");
        int b = readInt(scanner, "请输入第二个整数：");
        System.out.println(a + " + " + b + " = " + (a + b));

        scanner.close();
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("输入非法，请输入整数后重试。");
            } finally {
                System.out.println("finally：输入尝试结束。");
            }
        }
    }
}

