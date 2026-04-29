public class InheritanceDemo {
    public static void main(String[] args) {
        Employee e = new Employee(1, "Alice", 8000);
        Manager m = new Manager(2, "Bob", 12000, 3000);

        System.out.println("=== 同一个打印方法，打印不同类型对象 ===");
        printPerson(e);
        printPerson(m);
    }

    private static void printPerson(Person p) {
        System.out.println(p.toString());
    }
}

