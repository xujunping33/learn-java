import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Day75：Lambda + 方法引用（4 种）
 *
 * 1) 静态方法引用：ClassName::staticMethod
 * 2) 特定对象的实例方法引用：obj::method
 * 3) 特定类型任意对象的实例方法引用：ClassName::method
 * 4) 构造器引用：ClassName::new
 */
public class LambdaMethodRefDemo {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();
        students.add(new Student("tom", 88));
        students.add(new Student("alice", 95));
        students.add(new Student("bob", 72));

        System.out.println("=== 原始数据 ===");
        students.forEach(System.out::println);

        // 1) Comparator：Lambda 写法
        System.out.println();
        System.out.println("=== Lambda 排序（score desc）===");
        students.sort((a, b) -> Integer.compare(b.score(), a.score()));
        students.forEach(System.out::println);

        // 2) 静态方法引用：Integer::compare
        // 这里仍需要把参数顺序写出来（desc）
        System.out.println();
        System.out.println("=== 方法引用：静态方法 Integer::compare（score desc）===");
        students.sort((a, b) -> Integer.compare(b.score(), a.score())); // 等价于上面，只是强调 compare 是静态方法
        students.forEach(System.out::println);

        // 3) 特定类型任意对象实例方法引用：String::compareToIgnoreCase
        System.out.println();
        System.out.println("=== 方法引用：特定类型任意对象实例方法 String::compareToIgnoreCase（按 name 排序）===");
        students.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
        students.forEach(System.out::println);

        // 4) 特定对象实例方法引用：obj::method
        System.out.println();
        System.out.println("=== 方法引用：特定对象实例方法 (Locale.CHINA)::toString + String::toUpperCase ===");
        String prefix = "hello";
        Function<String, String> upper1 = s -> s.toUpperCase(Locale.ROOT);
        Function<String, String> upper2 = String::toUpperCase; // 任意 String 调用实例方法
        System.out.println("upper lambda: " + upper1.apply(prefix));
        System.out.println("upper method ref: " + upper2.apply(prefix));

        // 5) 构造器引用：Student::new
        System.out.println();
        System.out.println("=== 构造器引用：Student::new ===");
        Supplier<Student> supplier = Student::new;
        System.out.println("new student: " + supplier.get());

        // 6) 方法引用：System.out::println（特定对象实例方法引用）
        System.out.println();
        System.out.println("=== System.out::println（特定对象实例方法引用）===");
        students.forEach(System.out::println);
    }
}

/**
 * 用 record 让数据类更简洁（Java 16+）。
 * - 构造器引用 Student::new 会调用无参构造器，因此我们额外提供一个无参构造器（返回默认值）。
 */
record Student(String name, int score) {
    public Student() {
        this("noname", 0);
    }
}

