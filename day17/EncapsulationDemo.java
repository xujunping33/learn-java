public class EncapsulationDemo {
    public static void main(String[] args) {
        Person p = new Person(1, "Alice", 20);
        System.out.println("初始：" + p);

        System.out.println();
        System.out.println("尝试设置非法 name：\"\" ");
        boolean okName = p.setName("   ");
        System.out.println("setName 成功？" + okName);
        System.out.println("当前：" + p);

        System.out.println();
        System.out.println("尝试设置非法 age：-1 / 200");
        boolean okAge1 = p.setAge(-1);
        boolean okAge2 = p.setAge(200);
        System.out.println("setAge(-1) 成功？" + okAge1);
        System.out.println("setAge(200) 成功？" + okAge2);
        System.out.println("当前：" + p);

        System.out.println();
        System.out.println("设置合法值 name=Bob, age=30");
        p.setName("Bob");
        p.setAge(30);
        System.out.println("当前：" + p);
    }
}

