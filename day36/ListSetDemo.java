import java.util.ArrayList;
import java.util.HashSet;

public class ListSetDemo {
    public static void main(String[] args) {
        ArrayList<String> courses = new ArrayList<>();

        // 1) 添加
        courses.add("Java");
        courses.add("MySQL");
        courses.add("Java"); // 故意重复，后面给 Set 去重
        courses.add("Linux");
        System.out.println("添加后课程清单: " + courses);

        // 2) 查询
        System.out.println("包含 MySQL ? " + courses.contains("MySQL"));
        System.out.println("indexOf(Java) = " + courses.indexOf("Java"));

        // 3) 修改
        courses.set(1, "MySQL-8");
        System.out.println("修改后课程清单: " + courses);

        // 4) 删除
        courses.remove("Linux");
        System.out.println("删除后课程清单: " + courses);

        // 5) 遍历
        System.out.println("遍历课程:");
        for (String c : courses) {
            System.out.println("- " + c);
        }
        System.out.println("是否为空? " + courses.isEmpty());

        // 6) Set 去重
        HashSet<String> uniqueCourses = new HashSet<>(courses);
        System.out.println("HashSet 去重后: " + uniqueCourses);
    }
}

