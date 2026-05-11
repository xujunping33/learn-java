import java.util.Scanner;

public class ReverseArray {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        int n = in.readIntInRange("输入数组长度 N（1-1000）：", 1, 1000);
        int[] arr = new int[n];

        for (int i = 0; i < n; i++) {
            arr[i] = in.readInt("arr[" + i + "] = ");
        }

        reverseInPlace(arr);

        System.out.print("反转后：");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) System.out.print(" ");
            System.out.print(arr[i]);
        }
        System.out.println();

        scanner.close();
    }

    private static void reverseInPlace(int[] arr) {
        int l = 0;
        int r = arr.length - 1;
        while (l < r) {
            int t = arr[l];
            arr[l] = arr[r];
            arr[r] = t;
            l++;
            r--;
        }
    }
}

