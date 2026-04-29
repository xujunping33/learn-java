import java.util.Scanner;

public class LinearSearch {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        int n = in.readIntInRange("输入数组长度 N（1-1000）：", 1, 1000);
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = in.readInt("arr[" + i + "] = ");
        }

        int target = in.readInt("输入要查找的值：");
        int idx = indexOf(arr, target);
        System.out.println("index = " + idx);

        scanner.close();
    }

    private static int indexOf(int[] arr, int target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) return i;
        }
        return -1;
    }
}

