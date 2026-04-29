public class FinalMethodDemo {
    public static void main(String[] args) {
        Base b = new Base();
        Child c = new Child();
        System.out.println("b.id() = " + b.id());
        System.out.println("c.id() = " + c.id());
    }

    static class Base {
        public final String id() {
            return "Base";
        }
    }

    static class Child extends Base {
        // 如果你取消下面注释，会看到编译报错：Cannot override the final method from Base
        // @Override
        // public String id() {
        //     return "Child";
        // }
    }
}

