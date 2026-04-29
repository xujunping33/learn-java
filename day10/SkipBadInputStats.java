import java.util.Scanner;

public class SkipBadInputStats {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        System.out.println("=== SkipBadInputStats ===");
        System.out.println("输入若干整数（0-100 才算有效），输入 q 结束。");

        int count = 0;
        long sum = 0;

        while (true) {
            String line = in.readLine("> ").trim();
            if (line.equalsIgnoreCase("q")) break;
            if (line.isEmpty()) {
                System.out.println("空输入，跳过。");
                continue;
            }

            int v;
            try {
                v = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("非法输入，跳过。");
                continue; // 用 continue 简化嵌套
            }

            if (v < 0 || v > 100) {
                System.out.println("不在范围 0-100，跳过。");
                continue;
            }

            sum += v;
            count++;
        }

        if (count == 0) {
            System.out.println("没有有效数据。");
        } else {
            double avg = sum / (double) count;
            System.out.println("有效数量 = " + count);
            System.out.println("平均值 = " + avg);
        }

        scanner.close();
    }
}

