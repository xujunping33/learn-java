public class StaticDemo {
    public static void main(String[] args) {
        System.out.println("初始 Student.count = " + Student.getCount());

        Student a = new Student("Alice");
        System.out.println("创建 a: " + a);
        System.out.println("现在 Student.count = " + Student.getCount());

        Student b = new Student("Bob");
        System.out.println("创建 b: " + b);
        System.out.println("现在 Student.count = " + Student.getCount());

        System.out.println();
        System.out.println("验证：count 属于类，a/b 共享同一个计数器。");
        System.out.println("a.id=" + a.getId() + ", b.id=" + b.getId());
    }
}

