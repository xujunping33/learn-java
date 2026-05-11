public class Sorter {
    public interface ComparatorLike {
        // 返回负数：a 在前；返回 0：相等；返回正数：a 在后
        int compare(int a, int b);
    }

    public static void sort(int[] arr, ComparatorLike cmp) {
        // 简单实现：冒泡排序（用于演示策略切换）
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j + 1 < arr.length - i; j++) {
                if (cmp.compare(arr[j], arr[j + 1]) > 0) {
                    int t = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = t;
                }
            }
        }
    }
}

