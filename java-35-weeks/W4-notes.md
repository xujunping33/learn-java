# W4 Notes（每日 5 要点 + 3 个坑 + 1 个模板）

## Day 22（Object：所有类的父类）

### 5 条要点
- `toString()` 是调试/日志第一工具：让对象“可读”
- `==` 比较的是“引用”（是不是同一个对象）
- `equals()` 比较的是“逻辑相等”（需要类自己重写才有意义）
- `hashCode()` 和 `equals()` 必须保持契约：相等对象必须有相同 hashCode
- `hashCode/equals` 是 HashMap/HashSet 等哈希容器正常工作的基础

### 3 个坑
- 只重写 `equals` 不重写 `hashCode`：放进 HashSet/HashMap 会出奇怪问题
- 用 `==` 比较 `String` 内容：经常得到错误结果（应使用 `equals`）
- `toString` 不重写时打印成 `ClassName@xxxx`，信息量很低

### 1 个模板（equals + hashCode 一起重写）

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User)) return false;
    User other = (User) o;
    return id == other.id && java.util.Objects.equals(name, other.name);
}

@Override
public int hashCode() {
    return java.util.Objects.hash(id, name);
}
```

## Day 23（final：最终类/最终方法/最终变量）

### 5 条要点
- `final` 变量：引用不能再指向别的对象（常用于常量/不可变引用）
- `final` 方法：子类不能重写（锁定关键行为）
- `final` 类：不能被继承（常见于工具类/安全设计）
- 工具类常见写法：`final class` + `private` 构造器 + 全 `static` 方法
- `final` 的核心是“限制变化点”，让代码更可控

### 3 个坑
- `final int[] arr` 不代表数组内容不可变，只是 `arr` 这个引用不能换指向
- `final` 不是“线程安全”的代名词，它只限制重新赋值/重写/继承
- `final` 方法一旦设计不当，后续扩展会受限（所以要用在稳定的核心逻辑上）

### 1 个模板（工具类）

```java
public final class MathUtil {
    private MathUtil() {}
    public static int abs(int x) { return x >= 0 ? x : -x; }
}
```

## Day 24（常用注解：@Override / @Deprecated / @SuppressWarnings）

### 5 条要点
- `@Override`：让编译器验证“你真的重写了吗”（签名写错会直接报错）
- `@Deprecated`：表达“还能用，但不推荐再用”（提示使用者迁移）
- `@SuppressWarnings`：只在必要时、尽量局部（不要一刀切全文件）
- 注解是“给编译器/工具看的信息”，能减少低级错误
- 养成习惯：重写方法就写 `@Override`（低成本高收益）

### 3 个坑
- 以为重写了但签名不同（例如参数类型不一样）：没有 `@Override` 很难发现
- 滥用 `@SuppressWarnings`：会把真正的问题也掩盖掉
- `@Deprecated` 只是提示，不会阻止调用；要替代方案需另外提供新方法

### 1 个模板（重写就写 @Override）

```java
@Override
public String toString() {
    return "xxx";
}
```

## Day 25（单例模式：饿汉 vs 懒汉）

### 5 条要点
- 单例意图：全局只需要一个实例（配置/日志器/连接池等）
- 饿汉：类加载时就创建实例，简单、通常线程安全，但可能“用不到也创建”
- 懒汉：第一次调用才创建实例，节省资源，但要注意线程安全问题
- 单例基本写法：私有构造器 + 静态实例 + 静态 `getInstance()`
- 验证单例最直接：两次 `getInstance()` 比较 `==` 应为 `true`

### 3 个坑
- 懒汉基础版在多线程下可能创建多个实例（需要同步/双重检查等）
- 构造器没写 `private`，外部还能 `new`，单例就被破坏
- 把单例当“全局变量”滥用，会让代码耦合变高、难测试

### 1 个模板（饿汉）

```java
private static final EagerSingleton INSTANCE = new EagerSingleton();
public static EagerSingleton getInstance() { return INSTANCE; }
```

### 加练：线程安全懒汉（双重检查 DCL + volatile）

- **为什么要 `volatile`**：在某些情况下，`new` 的内部步骤可能发生重排，导致别的线程“看到 instance 不为 null，但对象还没初始化完”，从而出现非常隐蔽的 bug；`volatile` 用来禁止这种重排并保证可见性。

```java
private static volatile ThreadSafeLazySingleton instance;

public static ThreadSafeLazySingleton getInstance() {
    ThreadSafeLazySingleton local = instance;
    if (local == null) {
        synchronized (ThreadSafeLazySingleton.class) {
            local = instance;
            if (local == null) {
                local = new ThreadSafeLazySingleton();
                instance = local;
            }
        }
    }
    return local;
}
```

## Day 26（多态 + 转型：向上/向下转型与 instanceof）

### 5 条要点
- 向上转型：`Animal a = new Dog()`（更通用、可放入同一容器）
- 运行时多态：调用的是“实际对象”的重写方法（`a.speak()` 会跑 Dog/Cat 版本）
- 向下转型：`Dog d = (Dog) a` 有风险，必须确认实际类型
- 用 `instanceof` 做防御：`if (a instanceof Dog) { ... }`
- 面向父类/接口写循环：新增子类时循环代码尽量不改

### 3 个坑
- 不加 `instanceof` 直接强转，容易 `ClassCastException`
- 误解多态：变量类型是父类不代表“调用父类方法实现”，看的是对象实际类型
- 把子类特有逻辑写满 `if-else`：优先用重写/接口，只有确实需要时再向下转型

### 1 个模板（instanceof 防御 + 向下转型）

```java
if (a instanceof Dog) {
    ((Dog) a).fetch();
}
```

## Day 27（接口与抽象类：设计可扩展系统）

### 5 条要点
- 接口表达“能力/约定”：只关心能做什么（方法签名），不关心怎么做
- 多实现：同一个接口可以有多种实现（AliPay/WeChatPay/CashPay）
- 主流程面向接口：`Payment[]` + 循环调用 `pay()`，新增实现时主流程尽量不改
- 抽象类适合“需要共享字段/部分实现”的场景；否则优先接口
- 多态的价值：同一段代码处理不同实现（扩展靠新增类）

### 3 个坑
- 把 `if-else` 写在主流程里选择不同实现，会让扩展越来越痛苦
- 接口方法设计不合理会导致实现类不得不写很多无用代码
- 抽象类/接口别混用过度：先保持简单，够用再抽象

### 1 个模板（面向接口遍历调用）

```java
for (Payment p : methods) {
    p.pay(amount);
}
```

## Day 28（内部类：成员/静态/局部/匿名）

### 5 条要点
- 匿名内部类：最典型用途是“临时实现一个接口/抽象类”，只用一次就不单独建类
- 常用在“策略/回调”：把可变逻辑（比较规则、校验规则）作为参数传入
- 匿名内部类会生成一个“没有名字的实现类对象”，可以直接 `new 接口(){...}`
- 内部类让“逻辑靠近使用处”，减少类文件数量（但别滥用，复杂就拆类）
- 用接口参数 + 匿名内部类，可以做到“主流程不变，只换策略”

### 3 个坑
- 匿名内部类里访问的外部局部变量必须是“有效 final”（不要在外部再修改它）
- 逻辑太复杂写在匿名类里会很难读，复杂时还是拆成命名类
- 忘记写 `@Override` 容易把方法签名写错（尤其是接口方法）

### 1 个模板（匿名内部类实现接口）

```java
Sorter.sort(arr, new Sorter.ComparatorLike() {
    @Override
    public int compare(int a, int b) {
        return Integer.compare(a, b);
    }
});
```

