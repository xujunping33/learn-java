public class ObjectBasicsDemo {
    public static void main(String[] args) {
        System.out.println("== 1) == vs equals（自己重写 equals/hashCode 的类）==");
        User u1 = new User(1, "Alice");
        User u2 = new User(1, "Alice");

        System.out.println("u1 = " + u1);
        System.out.println("u2 = " + u2);
        System.out.println("u1 == u2 ? " + (u1 == u2));
        System.out.println("u1.equals(u2) ? " + u1.equals(u2));
        System.out.println("u1.hashCode() = " + u1.hashCode());
        System.out.println("u2.hashCode() = " + u2.hashCode());

        System.out.println();
        System.out.println("== 2) 默认 equals/toString（没有重写的类）==");
        PlainUser p1 = new PlainUser(1, "Alice");
        PlainUser p2 = new PlainUser(1, "Alice");
        System.out.println("p1.toString() = " + p1.toString());
        System.out.println("p2.toString() = " + p2.toString());
        System.out.println("p1 == p2 ? " + (p1 == p2));
        System.out.println("p1.equals(p2) ? " + p1.equals(p2));
        System.out.println("p1.hashCode() = " + p1.hashCode());
        System.out.println("p2.hashCode() = " + p2.hashCode());

        System.out.println();
        System.out.println("== 3) String 的 == vs equals 直觉 ==");
        String s1 = new String("hi");
        String s2 = new String("hi");
        System.out.println("s1 == s2 ? " + (s1 == s2));
        System.out.println("s1.equals(s2) ? " + s1.equals(s2));
    }
}

