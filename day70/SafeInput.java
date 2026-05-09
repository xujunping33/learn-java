import java.util.Scanner;

/**
 * 控制台输入工具：把“重复提示 + 校验 + 重试”收口到一处，Main 代码更干净。
 */
public class SafeInput {
    private final Scanner scanner;

    public SafeInput(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int readInt(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {
                System.out.println("请输入整数。");
            }
        }
    }

    public long readLong(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException e) {
                System.out.println("请输入整数（long）。");
            }
        }
    }
}

