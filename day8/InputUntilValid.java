import java.util.Scanner;

public class InputUntilValid {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        int x;
        do {
            x = in.readInt("输入整数（0-100 才能结束）：");
            if (x < 0 || x > 100) {
                System.out.println("不在范围内，继续输入。");
            }
        } while (x < 0 || x > 100);

        System.out.println("OK，输入结束：x=" + x);
        scanner.close();
    }
}

