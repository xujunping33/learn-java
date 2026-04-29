import java.util.InputMismatchException;
import java.util.Scanner;

public class TryCatchDemo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println();
            System.out.println("=== TryCatchDemo ===");
            System.out.println("输入两个整数 a b，计算 a / b（输入 q 退出）");
            System.out.print("a = ");

            if (!scanner.hasNext()) break;
            if (scanner.hasNext("q")) {
                scanner.next();
                break;
            }

            try {
                int a = scanner.nextInt();
                System.out.print("b = ");
                int b = scanner.nextInt();

                int result = a / b; // 可能触发 ArithmeticException
                System.out.println("结果：" + result);
            } catch (ArithmeticException e) {
                System.out.println("错误：除数不能为 0。");
            } catch (InputMismatchException e) {
                System.out.println("错误：请输入整数，不要输入字母/小数。");
                scanner.nextLine(); // 清掉错误输入
            } finally {
                System.out.println("finally：本轮结束。");
            }
        }

        scanner.close();
        System.out.println("程序结束。");
    }
}

