public class Dog extends Animal {
    public Dog(String name) {
        super(name);
    }

    @Override
    public void speak() {
        System.out.println(getName() + ": 汪汪！");
    }

    public void fetch() {
        System.out.println(getName() + " 正在叼回飞盘。");
    }
}

