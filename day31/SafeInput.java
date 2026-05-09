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

    public int readInt(String prompt) {
        while (true) {
            String line = readLine(prompt).trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("请输入整数。");
            }
        }
    }
}

