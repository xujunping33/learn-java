import java.util.Scanner;

public class SafeInput {
    private final Scanner scanner;

    public SafeInput(Scanner scanner) {
        this.scanner = scanner;
    }

    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            Integer v = tryReadInt(prompt);
            if (v == null) {
                System.out.println("请输入整数。");
                continue;
            }
            if (v < min || v > max) {
                System.out.println("请输入范围 " + min + " 到 " + max + " 的整数。");
                continue;
            }
            return v;
        }
    }

    public int readInt(String prompt) {
        while (true) {
            Integer v = tryReadInt(prompt);
            if (v != null) return v;
            System.out.println("请输入整数。");
        }
    }

    private Integer tryReadInt(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) return null;
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return null;
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

