import java.util.ArrayList;
import java.util.List;

public class GenericMethodsDemo {
    public static void main(String[] args) {
        System.out.println("=== Box<T> 测试 ===");
        Box<String> sBox = new Box<>("hello");
        System.out.println("sBox.get() = " + sBox.get());
        sBox.set("world");
        System.out.println("sBox.get() after set = " + sBox.get());

        Box<Integer> iBox = new Box<>(123);
        System.out.println("iBox.get() = " + iBox.get());

        System.out.println();
        System.out.println("=== Pair<K,V> 测试（可选） ===");
        Pair<Integer, String> pair = new Pair<>(1, "one");
        System.out.println(pair);

        System.out.println();
        System.out.println("=== 泛型方法 1：printArray ===");
        String[] words = {"Java", "Generic", "Demo"};
        printArray(words);

        Integer[] nums = {1, 2, 3, 4};
        printArray(nums);

        System.out.println();
        System.out.println("=== 泛型方法 2：firstOrNull（空列表处理） ===");
        List<String> empty = new ArrayList<>();
        System.out.println("empty firstOrNull = " + firstOrNull(empty));

        List<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");
        System.out.println("list firstOrNull = " + firstOrNull(list));
    }

    public static <T> void printArray(T[] arr) {
        if (arr == null) {
            System.out.println("<null>");
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) System.out.print(", ");
            System.out.print(arr[i]);
        }
        System.out.println();
    }

    public static <T> T firstOrNull(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(0);
    }
}

