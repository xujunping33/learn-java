import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Day76：Stream API Demo（创建流 + 中间操作 + 终止操作）
 *
 * 目标：用一份 List<Student> 覆盖常见 10+ 场景：
 * - filter / map / sorted / distinct / limit / skip
 * - collect(toList/toSet/toMap/groupingBy/joining/summarizingInt)
 * - anyMatch / allMatch / count
 */
public class StreamApiDemo {
    public static void main(String[] args) {
        List<Student> students = List.of(
                new Student(1, "alice", 95, 18),
                new Student(2, "bob", 72, 19),
                new Student(3, "carol", 88, 18),
                new Student(4, "dave", 59, 20),
                new Student(5, "erin", 95, 19),
                new Student(6, "bob", 72, 19) // 故意重复，用于 distinct/Set 演示
        );

        System.out.println("=== 0) 原始数据 ===");
        students.forEach(System.out::println);

        // 1) filter：过滤不及格
        System.out.println();
        System.out.println("=== 1) filter：不及格（score < 60）===");
        students.stream()
                .filter(s -> s.score() < 60)
                .forEach(System.out::println);

        // 2) map：提取 name 列表
        System.out.println();
        System.out.println("=== 2) map：提取 name ===");
        List<String> names = students.stream()
                .map(Student::name)
                .collect(Collectors.toList());
        System.out.println(names);

        // 3) distinct：去重（基于 equals/hashCode；record 默认基于全部字段）
        System.out.println();
        System.out.println("=== 3) distinct：去重后的学生数 ===");
        long distinctCount = students.stream().distinct().count();
        System.out.println("distinctCount = " + distinctCount);

        // 4) sorted：按 score desc 排序
        System.out.println();
        System.out.println("=== 4) sorted：按 score desc ===");
        students.stream()
                .sorted(Comparator.comparingInt(Student::score).reversed())
                .forEach(System.out::println);

        // 5) limit：Top3
        System.out.println();
        System.out.println("=== 5) limit：Top3（按 score desc）===");
        students.stream()
                .sorted(Comparator.comparingInt(Student::score).reversed())
                .limit(3)
                .forEach(System.out::println);

        // 6) skip：跳过前 2 个（配合排序更像分页）
        System.out.println();
        System.out.println("=== 6) skip：跳过前2个（按 id asc）===");
        students.stream()
                .sorted(Comparator.comparingLong(Student::id))
                .skip(2)
                .forEach(System.out::println);

        // 7) toSet：收集到 Set（演示去重）
        System.out.println();
        System.out.println("=== 7) collect(toSet)：去重后的 name 集合 ===");
        Set<String> nameSet = students.stream()
                .map(Student::name)
                .collect(Collectors.toSet());
        System.out.println(nameSet);

        // 8) toMap：id -> Student
        System.out.println();
        System.out.println("=== 8) collect(toMap)：id -> Student ===");
        Map<Long, Student> idMap = students.stream()
                .collect(Collectors.toMap(Student::id, s -> s));
        System.out.println("id=3 => " + idMap.get(3L));

        // 9) groupingBy：按 age 分组统计人数
        System.out.println();
        System.out.println("=== 9) groupingBy：按 age 分组人数 ===");
        Map<Integer, Long> ageCount = students.stream()
                .collect(Collectors.groupingBy(Student::age, Collectors.counting()));
        System.out.println(ageCount);

        // 10) joining：把 name 拼成一行
        System.out.println();
        System.out.println("=== 10) joining：拼接 name ===");
        String joined = students.stream()
                .map(Student::name)
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));
        System.out.println(joined);

        // 11) summarizingInt：统计 score（count/sum/min/max/avg）
        System.out.println();
        System.out.println("=== 11) summarizingInt：score 统计 ===");
        IntSummaryStatistics stats = students.stream()
                .collect(Collectors.summarizingInt(Student::score));
        System.out.println(stats);

        // 12) anyMatch / allMatch：布尔判断
        System.out.println();
        System.out.println("=== 12) anyMatch/allMatch ===");
        boolean anyFail = students.stream().anyMatch(s -> s.score() < 60);
        boolean allAdult = students.stream().allMatch(s -> s.age() >= 18);
        System.out.println("anyFail=" + anyFail + ", allAdult=" + allAdult);
    }
}

/**
 * 用 record 作为数据源（简洁 + 自动 equals/hashCode，方便 distinct 演示）
 */
record Student(long id, String name, int score, int age) {}

