import java.util.Random;
import java.util.Scanner;

public class GuessNumberGame {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        System.out.println("=== GuessNumberGame ===");
        int secret = new Random().nextInt(100) + 1; // 1..100
        int maxTries = 7;

        System.out.println("我想了一个 1-100 的数，你有 " + maxTries + " 次机会。");

        for (int attempt = 1; attempt <= maxTries; attempt++) {
            int guess = in.readIntInRange("第 " + attempt + " 次猜（1-100）：", 1, 100);

            if (guess == secret) {
                System.out.println("恭喜猜对了！答案是 " + secret + "。");
                break; // 结束循环
            }

            if (guess < secret) System.out.println("小了。");
            else System.out.println("大了。");

            if (attempt == maxTries) {
                System.out.println("次数用完了。答案是 " + secret + "。");
            }
        }

        scanner.close();
    }
}

