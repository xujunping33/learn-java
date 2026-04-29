import java.util.Scanner;

public class PaymentDemo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Payment[] methods = new Payment[] {
                new AliPay(),
                new WeChatPay(),
                new CashPay()
        };

        System.out.println("=== PaymentDemo ===");
        double amount = readAmount(scanner);

        for (Payment p : methods) {
            System.out.println();
            System.out.println("使用 " + p.name() + " 支付：");
            boolean ok = p.pay(amount);
            System.out.println("结果：" + (ok ? "OK" : "FAIL"));
        }

        scanner.close();
    }

    private static double readAmount(Scanner scanner) {
        while (true) {
            System.out.print("输入支付金额（>0）：");
            String line = scanner.nextLine().trim();
            try {
                double v = Double.parseDouble(line);
                if (v <= 0) {
                    System.out.println("金额必须 > 0");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("请输入数字。");
            }
        }
    }
}

