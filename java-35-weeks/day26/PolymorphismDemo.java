public class PolymorphismDemo {
    public static void main(String[] args) {
        Animal[] animals = new Animal[] {
                new Dog("旺财"),
                new Cat("咪咪"),
                new Dog("大黄")
        };

        System.out.println("== 1) 向上转型 + 运行时多态（调用实际对象的重写方法）==");
        for (Animal a : animals) {
            a.speak(); // Dog/Cat 会走各自的 speak()
        }

        System.out.println();
        System.out.println("== 2) 仅在必要时向下转型：Dog 才有 fetch() ==");
        for (Animal a : animals) {
            if (a instanceof Dog) {
                Dog d = (Dog) a; // 向下转型
                d.fetch();
            } else {
                System.out.println(a.getName() + " 不是狗，跳过 fetch()");
            }
        }

        System.out.println();
        System.out.println("== 3) 为什么需要 instanceof：避免 ClassCastException ==");
        Animal x = new Cat("小黑");
        // Dog wrong = (Dog) x; // 取消注释会在运行时报 ClassCastException
        System.out.println("x 实际类型是 " + x.getClass().getSimpleName());
    }
}

