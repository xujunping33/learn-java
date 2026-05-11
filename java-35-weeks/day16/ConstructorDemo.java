public class ConstructorDemo {
    public static void main(String[] args) {
        Student s1 = new Student(1, "Alice");
        Student s2 = new Student(2, "Bob", 88);

        System.out.println("=== 构造器创建的对象 ===");
        System.out.println(s1);
        System.out.println(s2);

        // 对比：如果没有构造器，你需要先 new 再逐个赋值（容易漏字段）
        Student s3 = new Student(3, "Charlie");
        s3.score = 60;
        System.out.println("=== 修改字段后的对象 ===");
        System.out.println(s3);
    }
}

