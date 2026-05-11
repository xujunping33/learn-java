public class BookDemo {
    public static void main(String[] args) {
        Book b1 = new Book();
        b1.id = 1;
        b1.title = "Java 入门";
        b1.author = "Tom";
        b1.price = 59.9;

        Book b2 = new Book();
        b2.id = 2;
        b2.title = "算法基础";
        b2.author = "Alice";
        b2.price = 88.0;

        Book b3 = new Book();
        b3.id = 3;
        b3.title = "数据库入门";
        b3.author = "Bob";
        b3.price = 66.6;

        // 修改字段演示（Day15 先不封装）
        b3.price = 69.9;

        System.out.println("=== 所有图书 ===");
        System.out.println(b1);
        System.out.println(b2);
        System.out.println(b3);

        Book mostExpensive = maxPrice(b1, b2, b3);
        System.out.println();
        System.out.println("=== 最贵的一本 ===");
        System.out.println(mostExpensive);
    }

    private static Book maxPrice(Book a, Book b, Book c) {
        Book best = a;
        if (b.price > best.price) best = b;
        if (c.price > best.price) best = c;
        return best;
    }
}

