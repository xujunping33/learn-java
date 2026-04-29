import java.util.Scanner;

public class MemberDiscount {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("是否会员（true/false）：");
        boolean member = scanner.nextBoolean();
        System.out.print("消费金额：");
        double amount = scanner.nextDouble();

        double rate = member ? 0.9 : 1.0;
        double pay = amount * rate;
        System.out.println("应付金额 = " + pay);
        scanner.close();
    }
}

