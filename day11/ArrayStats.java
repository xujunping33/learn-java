import java.util.Scanner;

public class ArrayStats {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        int n = in.readIntInRange("输入成绩数量 N（1-1000）：", 1, 1000);
        int[] scores = new int[n];

        for (int i = 0; i < n; i++) {
            scores[i] = in.readIntInRange("第 " + (i + 1) + " 个成绩（0-100）：", 0, 100);
        }

        int max = scores[0];
        int min = scores[0];
        long sum = 0;
        int fail = 0;

        for (int x : scores) {
            if (x > max) max = x;
            if (x < min) min = x;
            sum += x;
            if (x < 60) fail++;
        }

        double avg = sum / (double) n;
        System.out.println("max = " + max);
        System.out.println("min = " + min);
        System.out.println("avg = " + avg);
        System.out.println("不及格人数 = " + fail);

        scanner.close();
    }
}

