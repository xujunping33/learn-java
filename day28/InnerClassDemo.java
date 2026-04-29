import java.util.Arrays;

public class InnerClassDemo {
    public static void main(String[] args) {
        int[] data = {3, -1, 10, 0, -7, 2};
        System.out.println("原始数据: " + Arrays.toString(data));

        int[] asc = data.clone();
        Sorter.sort(asc, new Sorter.ComparatorLike() {
            @Override
            public int compare(int a, int b) {
                return Integer.compare(a, b);
            }
        });
        System.out.println("升序:     " + Arrays.toString(asc));

        int[] desc = data.clone();
        Sorter.sort(desc, new Sorter.ComparatorLike() {
            @Override
            public int compare(int a, int b) {
                return Integer.compare(b, a);
            }
        });
        System.out.println("降序:     " + Arrays.toString(desc));

        int[] absAsc = data.clone();
        Sorter.sort(absAsc, new Sorter.ComparatorLike() {
            @Override
            public int compare(int a, int b) {
                int ca = Math.abs(a);
                int cb = Math.abs(b);
                int r = Integer.compare(ca, cb);
                if (r != 0) return r;
                return Integer.compare(a, b); // 绝对值相同则按原值升序
            }
        });
        System.out.println("按绝对值升序: " + Arrays.toString(absAsc));
    }
}

