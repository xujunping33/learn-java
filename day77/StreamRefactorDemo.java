import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Day77：把“学生统计”写两版（for vs Stream）并对比。
 *
 * 统计项：
 * - 平均分
 * - 最高分学生
 * - 按年龄分组人数
 * - Top3（按分数降序）
 *
 * 要点：
 * - 空列表要处理（避免除 0、Optional 空等问题）
 * - 输出对齐：两版结果应一致
 */
public class StreamRefactorDemo {
    public static void main(String[] args) {
        List<Student> students = List.of(
                new Student(1, "alice", 95, 18),
                new Student(2, "bob", 72, 19),
                new Student(3, "carol", 88, 18),
                new Student(4, "dave", 59, 20),
                new Student(5, "erin", 95, 19)
        );

        System.out.println("=== 数据 ===");
        students.forEach(System.out::println);

        System.out.println();
        System.out.println("=== 1) for 版 ===");
        Stats s1 = calcStatsFor(students);
        System.out.println(s1);

        System.out.println();
        System.out.println("=== 2) stream 版 ===");
        Stats s2 = calcStatsStream(students);
        System.out.println(s2);
    }

    // ---------------------------
    // for 循环版
    // ---------------------------
    static Stats calcStatsFor(List<Student> list) {
        if (list == null || list.isEmpty()) {
            return Stats.empty();
        }

        double sum = 0;
        Student max = list.get(0);
        Map<Integer, Long> ageCount = new HashMap<>();
        List<Student> top = new ArrayList<>();

        for (Student s : list) {
            sum += s.score();

            if (s.score() > max.score()) {
                max = s;
            }

            ageCount.put(s.age(), ageCount.getOrDefault(s.age(), 0L) + 1);
        }

        // Top3：先复制一份再排序（避免改原列表）
        List<Student> copy = new ArrayList<>(list);
        copy.sort((a, b) -> Integer.compare(b.score(), a.score()));
        for (int i = 0; i < Math.min(3, copy.size()); i++) {
            top.add(copy.get(i));
        }

        double avg = sum / list.size();
        return new Stats(avg, max, ageCount, top);
    }

    // ---------------------------
    // Stream 版
    // ---------------------------
    static Stats calcStatsStream(List<Student> list) {
        if (list == null || list.isEmpty()) {
            return Stats.empty();
        }

        DoubleSummaryStatistics scoreStats = list.stream()
                .collect(Collectors.summarizingDouble(Student::score));

        Optional<Student> max = list.stream()
                .max(Comparator.comparingInt(Student::score));

        Map<Integer, Long> ageCount = list.stream()
                .collect(Collectors.groupingBy(Student::age, Collectors.counting()));

        List<Student> top3 = list.stream()
                .sorted(Comparator.comparingInt(Student::score).reversed())
                .limit(3)
                .collect(Collectors.toList());

        return new Stats(scoreStats.getAverage(), max.orElse(null), ageCount, top3);
    }
}

record Student(long id, String name, int score, int age) {}

record Stats(double avgScore, Student maxScoreStudent, Map<Integer, Long> ageCount, List<Student> top3) {
    static Stats empty() {
        return new Stats(0.0, null, Map.of(), List.of());
    }
}

