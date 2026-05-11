import java.util.Scanner;

public class SafeInput {
    private final Scanner scanner;

    public SafeInput(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) return "";
        return scanner.nextLine();
    }

    public String readNonEmptyLine(String prompt) {
        while (true) {
            String s = readLine(prompt).trim();
            if (!s.isEmpty()) return s;
            System.out.println("输入不能为空，请重试。");
        }
    }

    public int readInt(String prompt) {
        while (true) {
            String s = readLine(prompt).trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("请输入整数。");
            }
        }
    }

    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int v = readInt(prompt);
            if (v < min || v > max) {
                System.out.println("请输入范围 " + min + " 到 " + max + " 的整数。");
                continue;
            }
            return v;
        }
    }

    public double readDouble(String prompt) {
        while (true) {
            String s = readLine(prompt).trim();
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                System.out.println("请输入数字（可带小数）。");
            }
        }
    }
}

