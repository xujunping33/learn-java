package learn.java.servlet.model;

/** Day121+：学生实体；Day124 增加 {@code phone}。 */
public class Student {

    public long id;
    public String name;
    public int score;
    public int age;
    public String phone;

    public Student() {}

    public Student(long id, String name, int score, int age, String phone) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.age = age;
        this.phone = phone;
    }
}
