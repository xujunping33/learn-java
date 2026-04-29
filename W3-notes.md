# W3 Notes（每日 5 要点 + 3 个坑 + 1 个模板）

## Day 15（类与对象：从“会用”到“会设计”）

### 5 条要点
- 类是“模板”，对象是“实例”：`new Book()` 会在内存里生成一个对象
- 字段负责保存数据（状态），方法负责行为（逻辑）
- 别把所有逻辑堆到 `main`：对象相关逻辑放到类/方法里更清晰可复用
- `toString()` 让对象“可打印”，对调试非常有用
- 先把程序跑通再逐步改进：Day16/17 会加入构造器与封装

### 3 个坑
- 忘了重写 `toString()`：打印对象会变成 `Book@xxxx`（不好读）
- 把“找最贵”逻辑写重复：最好抽一个 `maxPrice(...)` 方法
- `double` 比较要注意精度（本阶段先用 `>` 够用，后面再深入）

### 1 个模板（遍历找最贵）

```java
Book best = books[0];
for (int i = 1; i < books.length; i++) {
    if (books[i].price > best.price) best = books[i];
}
```

## Day 16（构造方法：初始化策略 + 重载）

### 5 条要点
- 构造器用于初始化对象：让对象“从出生就有合理状态”
- 构造器可以重载：同名不同参（不同初始化方式）
- `this(...)` 可以在构造器里调用另一个构造器，减少重复代码
- “默认值策略”要明确：本次 `score` 默认用 0（表示未录入/或初始为 0）
- `toString()` 配合构造器，能快速看出对象初始化是否正确

### 3 个坑
- 构造器里忘记给字段赋值，会产生半初始化对象（后面封装会更严格）
- `this(...)` 必须是构造器第一行
- 重载不能只靠返回值区分（构造器本来就没返回值）

### 1 个模板（构造器链：this(...)）

```java
public Student(int id, String name) {
    this(id, name, 0);
}
```

## Day 17（封装：private + getter/setter + 校验）

### 5 条要点
- `private` 把字段藏起来：外部不能直接改，必须走类提供的入口
- setter 的核心价值：把“合法性规则”集中在一个地方（只要改一处）
- 能只读就只读：比如 `id` 通常只给 getter，不给 setter
- 构造器也应该复用校验（例如构造器里调用 `setAge/setName`）
- 封装的目标是“维护不变量”（例如 age 永远在 0–150），不是为了写 getter/setter

### 3 个坑
- setter 不校验就等于没封装（外部仍然能把对象改坏）
- 只校验但不告诉调用者结果：最好返回 `boolean` 或抛异常（本次用 boolean）
- `name.trim()` 的位置写错：要先判空再 trim（否则空指针）

### 1 个模板（setter 校验并返回是否成功）

```java
public boolean setAge(int age) {
    if (age < 0 || age > 150) return false;
    this.age = age;
    return true;
}
```

## Day 18（包 package：让代码规模化）

### 5 条要点
- `package` 决定“类的全限定名”：例如 `com.xjp.sms.app.Main`
- 文件夹路径必须匹配包名：`com/xjp/sms/app/Main.java` 里就必须写 `package com.xjp.sms.app;`
- 跨包使用类需要 `import`（或写全限定名）
- 编译推荐用 `-d out`：让 `.class` 按包结构输出到 `out/`
- 运行用 classpath 指向编译输出目录：`java -cp out com.xjp.sms.app.Main`

### 3 个坑
- `package` 写了但目录不对应：编译/运行会找不到类
- 运行时 `java Main` 不行了：必须用全限定名 `com.xjp...Main`
- classpath 指错（应该指到 `out`，而不是 `out/com/...`）

### 1 个模板（终端编译运行 package 工程）

```bash
mkdir -p out
javac -d out com/xjp/sms/model/Student.java com/xjp/sms/util/SafeInput.java \
  com/xjp/sms/service/StudentService.java com/xjp/sms/app/Main.java
java -cp out com.xjp.sms.app.Main
```

## Day 19（static：类变量/类方法的边界）

### 5 条要点
- `static` 属于“类”，不属于“某个对象”：所有对象共享同一份
- 适合放：常量、工具方法、全局计数器（但要谨慎）
- 用 `ClassName.staticMember` 访问更清晰（例如 `Student.getCount()`）
- `static` 方法里不能直接用实例成员（没有 `this`）
- 共享意味着“容易互相影响”：长期运行/并发场景要更谨慎（先理解“共享”即可）

### 3 个坑
- 把本该属于每个对象的字段写成 static，会导致所有对象互相覆盖
- 静态计数器如果忘记在构造器里更新，统计就不准
- `static` 的自增 id 生成器是全局状态：重启程序会重置；并发下需要额外处理

### 1 个模板（静态自增 id）

```java
private static int nextId = 1;
public static int next() {
    return nextId++;
}
```

## Day 20（继承：is-a 关系 + 方法重写）

### 5 条要点
- 继承表达 is-a：`Employee` 是一种 `Person`，所以 `Employee extends Person`
- 子类会“继承”父类的公开行为（如 `getId/getName`），并可以新增自己的字段/方法
- 方法重写：子类提供同名同参实现，用 `@Override` 防止写错签名
- 多态直观体验：`Person p = new Manager(...)` 也能打印出 `Manager.toString()`
- 什么时候用继承：当模型天然是“种类”关系；否则很多时候组合更合适（后面再展开）

### 3 个坑
- 忘了写 `@Override`，签名写错就变成“新方法”而不是重写
- 父类字段建议 `private`，通过 getter 暴露（否则子类随便改更难维护）
- 滥用继承会导致层级过深、职责混乱（看起来复用，实际更难改）

### 1 个模板（子类构造器调用父类构造器）

```java
public Employee(int id, String name, double baseSalary) {
    super(id, name);
    this.baseSalary = baseSalary;
}
```

## Day 21（super：复用父类逻辑 + 大作业：部门员工管理）

### 5 条要点
- `super(...)`：子类构造器里调用父类构造器，初始化“父类那部分”
- `super.method()`：在子类“增强”父类实现时很有用（例如经理工资 = 父类工资 + bonus）
- 多态：用 `Employee` 引用也能调用到 `Manager` 的重写方法（如 `calcSalary()`）
- 分层依然重要：`Main` 负责菜单，`DepartmentService` 负责增删查列表
- 继承 + 封装一起用更稳：父类字段 `private`，通过 getter 给子类/外部读取

### 3 个坑
- 把工资计算写死在 `Main`，会让“普通员工/经理”差异难扩展
- 误以为 `super` 是“父类对象”：它只是调用父类实现的一种语法
- `super(...)` 必须写在子类构造器第一行

### 1 个模板（经理工资：重写 + super.method）

```java
@Override
public double calcSalary() {
    return super.calcSalary() + bonus;
}
```

