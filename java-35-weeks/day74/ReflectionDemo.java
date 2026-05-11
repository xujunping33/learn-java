import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Day74：反射 Demo
 *
 * 演示点：
 * 1) 反射创建对象（Constructor）
 * 2) 反射调用方法（Method）
 * 3) 反射读取/修改字段（Field + setAccessible）
 */
public class ReflectionDemo {
    public static void main(String[] args) throws Exception {
        // 1) 拿到 Class（两种常见方式：Class.forName / Xxx.class / obj.getClass）
        Class<?> clazz = Class.forName("Student");
        System.out.println("[1] Class = " + clazz.getName());

        // 2) 通过构造器创建对象
        Constructor<?> ctor = clazz.getConstructor(String.class, int.class);
        System.out.println("[2] Constructor = " + ctor);
        Object stu = ctor.newInstance("tom", 80);
        System.out.println("created: " + stu);

        // 3) 通过 Method 调用 public 方法
        Method setScore = clazz.getMethod("setScore", int.class);
        System.out.println("[3] Method(setScore) = " + setScore);
        setScore.invoke(stu, 95);

        Method getScore = clazz.getMethod("getScore");
        System.out.println("[3] Method(getScore) = " + getScore);
        Object score = getScore.invoke(stu);
        System.out.println("score via reflection: " + score);

        // 4) 访问 private 字段（演示：仅学习用）
        Field secret = clazz.getDeclaredField("secretNote");
        System.out.println("[4] Field(secretNote) = " + secret);
        secret.setAccessible(true); // 关闭访问检查（会破坏封装，所以生产环境慎用）
        System.out.println("secret before: " + secret.get(stu));
        secret.set(stu, "changed by reflection");
        System.out.println("secret after: " + secret.get(stu));

        System.out.println("final: " + stu);
    }
}

/**
 * 反射目标类：为了演示，把它放在同一个文件（默认包）。
 * 你也可以把它拆成单独文件，只要类名一致即可。
 */
class Student {
    private String name;
    private int score;
    private String secretNote = "init secret";

    public Student(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", score=" + score +
                ", secretNote='" + secretNote + '\'' +
                '}';
    }
}

