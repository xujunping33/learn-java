import java.util.Scanner;

public class SquareCube {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("输入一个整数 n：");
        long n = scanner.nextLong();
        System.out.println("n^2 = " + (n * n));
        System.out.println("n^3 = " + (n * n * n));
        scanner.close();
    }
}

