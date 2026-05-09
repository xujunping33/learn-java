import java.util.HashMap;
import java.util.Map;

public class MapDemo {
    public static void main(String[] args) {
        Map<Integer, Student> students = new HashMap<>();

        // 新增
        addStudent(students, new Student(101, "Alice", 88));
        addStudent(students, new Student(102, "Bob", 59));
        addStudent(students, new Student(103, "Cathy", 95));
        addStudent(students, new Student(101, "Dup", 60)); // 重复 id 演示

        // 按 id 查询
        queryById(students, 102);
        queryById(students, 999);

        // 删除
        removeById(students, 102);
        removeById(students, 999);

        // 遍历打印（entrySet）
        System.out.println();
        System.out.println("=== 遍历打印（entrySet）===");
        for (Map.Entry<Integer, Student> e : students.entrySet()) {
            System.out.println("key=" + e.getKey() + ", value=" + e.getValue());
        }

        // keySet 遍历
        System.out.println();
        System.out.println("=== 遍历打印（keySet）===");
        for (Integer id : students.keySet()) {
            System.out.println("id=" + id + ", student=" + students.get(id));
        }

        // 统计
        printStats(students);
    }

    private static void addStudent(Map<Integer, Student> map, Student s) {
        if (map.containsKey(s.getId())) {
            System.out.println("新增失败：id 已存在 -> " + s.getId());
            return;
        }
        map.put(s.getId(), s);
        System.out.println("新增成功：" + s);
    }

    private static void queryById(Map<Integer, Student> map, int id) {
        Student s = map.get(id);
        if (s == null) {
            System.out.println("查询 id=" + id + "：未找到");
        } else {
            System.out.println("查询 id=" + id + "：" + s);
        }
    }

    private static void removeById(Map<Integer, Student> map, int id) {
        Student removed = map.remove(id);
        if (removed == null) {
            System.out.println("删除 id=" + id + "：未找到");
        } else {
            System.out.println("删除 id=" + id + "：成功");
        }
    }

    private static void printStats(Map<Integer, Student> map) {
        System.out.println();
        System.out.println("=== 统计 ===");
        if (map.isEmpty()) {
            System.out.println("暂无数据");
            return;
        }

        long sum = 0;
        Student max = null;
        Student min = null;

        for (Student s : map.values()) {
            sum += s.getScore();
            if (max == null || s.getScore() > max.getScore()) max = s;
            if (min == null || s.getScore() < min.getScore()) min = s;
        }

        double avg = sum / (double) map.size();
        System.out.println("平均分 = " + avg);
        System.out.println("最高分 = " + max);
        System.out.println("最低分 = " + min);
    }
}

