import java.util.Scanner;

public class MatrixTranspose {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        System.out.println("=== MatrixTranspose ===");
        int n = in.readIntInRange("输入方阵大小 n（1-10）：", 1, 10);
        int[][] a = new int[n][n];

        System.out.println("输入矩阵元素：");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] = in.readIntInRange("a[" + i + "][" + j + "] = ", -1000000, 1000000);
            }
        }

        System.out.println();
        System.out.println("原矩阵：");
        print(a);

        transposeInPlace(a);

        System.out.println();
        System.out.println("转置后：");
        print(a);

        scanner.close();
    }

    private static void transposeInPlace(int[][] a) {
        int n = a.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int t = a[i][j];
                a[i][j] = a[j][i];
                a[j][i] = t;
            }
        }
    }

    private static void print(int[][] a) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                if (j > 0) System.out.print(" ");
                System.out.print(a[i][j]);
            }
            System.out.println();
        }
    }
}

