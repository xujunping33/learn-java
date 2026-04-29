import java.util.ArrayList;
import java.util.List;

public class AnnotationDemo {
    public static void main(String[] args) {
        System.out.println("== 1) @Override：让编译器帮你抓“没真的重写” ==");
        Parent p = new Parent();
        Child c = new Child();
        System.out.println("p.greet(\"Tom\") = " + p.greet("Tom"));
        System.out.println("c.greet(\"Tom\") = " + c.greet("Tom"));

        // 取消下面注释，观察编译错误：方法签名对不上，所以无法 @Override
        // c.greet(123);

        System.out.println();
        System.out.println("== 2) @Deprecated：表达“不推荐再用” ==");
        legacyApi();

        System.out.println();
        System.out.println("== 3) @SuppressWarnings：只在必要且局部使用 ==");
        suppressWarningsExample();
    }

    static class Parent {
        public String greet(String name) {
            return "Hello, " + name;
        }
    }

    static class Child extends Parent {
        @Override
        public String greet(String name) {
            return "Hi, " + name;
        }

        // 故意“签名写错”的例子：
        // 如果你以为这是重写 greet(String)，其实不是（参数类型不同）。
        // 加上 @Override 会立刻编译报错，避免踩坑。
        //
        // @Override
        public String greet(int x) {
            return "greet(int) x=" + x;
        }
    }

    @Deprecated
    public static void legacyApi() {
        System.out.println("这是一个 @Deprecated 方法：还能用，但不推荐。");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void suppressWarningsExample() {
        List raw = new ArrayList(); // 原生类型（只是演示，不推荐）
        raw.add("ok");
        raw.add(123);
        System.out.println("raw size=" + raw.size());
    }
}

