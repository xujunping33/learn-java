import java.util.Scanner;

public class ArrayStatsRefactored {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        int n = in.readIntInRange("输入成绩数量 N（1-1000）：", 1, 1000);
        int[] scores = readScores(in, n);

        System.out.println("max = " + max(scores));
        System.out.println("min = " + min(scores));
        System.out.println("avg = " + avg(scores));
        System.out.println("不及格人数 = " + countFail(scores, 60));

        scanner.close();
    }

    private static int[] readScores(SafeInput in, int n) {
        int[] scores = new int[n];
        for (int i = 0; i < n; i++) {
            scores[i] = in.readIntInRange("第 " + (i + 1) + " 个成绩（0-100）：", 0, 100);
        }
        return scores;
    }

    public static int max(int[] arr) {
        int m = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > m) m = arr[i];
        }
        return m;
    }

    public static int min(int[] arr) {
        int m = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < m) m = arr[i];
        }
        return m;
    }

    public static double avg(int[] arr) {
        long sum = 0;
        for (int x : arr) sum += x;
        return sum / (double) arr.length;
    }

    public static int countFail(int[] arr, int passLine) {
        int cnt = 0;
        for (int x : arr) {
            if (x < passLine) cnt++;
        }
        return cnt;
    }
}

