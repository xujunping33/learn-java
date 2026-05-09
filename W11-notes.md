# W11 Notes（每日 5 要点 + 3 个坑 + 1 个模板）

## Day 71（Maven 入门：创建项目 + 生命周期）

### 5 条要点
- Maven 的价值：依赖管理 + 统一构建 + 可复现（同一份 `pom.xml` 在任何机器可重建）
- 标准目录结构：`src/main/java`（业务代码）与 `src/test/java`（测试代码）
- 生命周期常用命令：`clean`（清理）、`compile`（编译）、`test`（测试）、`package`（打包）
- `pom.xml` 核心字段：`groupId/artifactId/version`（唯一标识一个构件）
- 插件也是“依赖”：例如 `exec-maven-plugin` 让你可以 `mvn exec:java` 运行 main

### 3 个坑
- 机器没装 Maven：`mvn` 命令找不到（需要安装 Maven 或使用 Maven Wrapper）
- 包名/目录结构不一致：导致类找不到（Java 包名要和目录对应）
- 只会点 IDE 运行：不会命令行构建时，换机器/CI 就容易卡住

### 1 个模板（最小 Maven 项目结构）

```text
maven-demo/
  pom.xml
  src/
    main/
      java/
        your/package/App.java
    test/
      java/
```

## Day 72（Maven 依赖 + JUnit5 单测）

### 5 条要点
- 测试代码目录：`src/test/java`（只在 test 阶段编译/运行）
- JUnit5 依赖通常 scope= `test`：只用于测试，不会打进生产代码
- `mvn test` 会自动跑 surefire 插件：发现并执行 `*Test` 类
- 单测要覆盖：正常情况 + 边界情况 + 异常情况（至少各 1 个）
- 单测失败的价值：你能快速定位哪里坏了，修改更有信心

### 3 个坑
- 依赖没加或 scope 写错：测试类编译不过（找不到 junit 包）
- 测试类命名不规范：不叫 `*Test` 可能不会被自动发现
- 只写“快乐路径”：不测边界/异常，改代码时仍然容易踩坑

### 1 个模板（断言）

```java
assertEquals(3, 1 + 2);
assertThrows(IllegalArgumentException.class, () -> doSomething(null));
```

## Day 73（工厂模式：简单工厂 vs 工厂方法）

### 5 条要点
- 工厂模式的动机：把“创建对象（new）”的逻辑集中起来，调用方只关心接口
- 简单工厂：一个工厂方法根据参数返回不同实现（上手快）
- 工厂方法：每种产品对应一个工厂（新增类型时更容易扩展，减少改动面）
- 多态的配合：调用方只依赖 `Payment` 接口，不依赖 `AliPay/WeChatPay` 具体类
- 选择建议：类型少/变化小用简单工厂；类型多/频繁扩展用工厂方法更稳

### 3 个坑
- 简单工厂的 if/switch 越写越长：新增类型必改工厂，容易违背开闭原则
- 把业务逻辑塞进工厂：工厂只负责创建，不要顺便做支付/校验等业务
- 过度设计：很小的项目不必强行上复杂模式，先把边界和职责搞清楚

### 1 个模板（工厂方法接口）

```java
interface Product {}
interface Factory { Product create(); }
```

## Day 74（反射：Class/Field/Method/Constructor）

### 5 条要点
- 反射的价值：在运行时获取类信息并操作它（框架常用：IoC/ORM/注解处理）
- 获取 `Class`：`Class.forName` / `Xxx.class` / `obj.getClass()`
- 创建对象：`Constructor.newInstance(...)`
- 调用方法：`Method.invoke(obj, args...)`
- 访问私有成员：`getDeclaredField/Method` + `setAccessible(true)`（学习理解即可，生产慎用）

### 3 个坑
- 反射性能更差、可维护性更低：只在需要“动态能力”时使用
- 方法/字段名写错：运行时才报错（编译期不会提示）
- `setAccessible(true)` 破坏封装：可能引入安全/兼容问题

### 1 个模板（反射调用方法）

```java
Class<?> c = Class.forName("Foo");
Object obj = c.getConstructor().newInstance();
Method m = c.getMethod("bar", String.class);
m.invoke(obj, "x");
```

## Day 75（Lambda + 方法引用）

### 5 条要点
- Lambda：把“方法实现”当作参数传递（本质是函数式接口的实现）
- 常见场景：`Comparator` 排序、集合遍历、回调等
- 方法引用是 Lambda 的简写（前提：参数列表与返回值能对上）
- 4 种方法引用：
  - 静态：`ClassName::staticMethod`
  - 特定对象实例：`obj::method`
  - 特定类型任意对象实例：`ClassName::method`
  - 构造器：`ClassName::new`
- 选择建议：更清晰就用哪个；过度“炫技”会降低可读性

### 3 个坑
- 不是所有 Lambda 都能改成方法引用：签名对不上就不行
- 容易混淆两种实例方法引用：`obj::m` vs `Class::m`
- 捕获外部变量（闭包）要注意：变量必须是 effectively final（不能反复赋值）

### 1 个模板（Comparator）

```java
list.sort((a, b) -> Integer.compare(a.getScore(), b.getScore()));
list.forEach(System.out::println);
```

## Day 76（Stream API：创建/中间/终止）

### 5 条要点
- Stream 是“数据处理管道”：数据源 → 一串中间操作 → 一个终止操作出结果
- 中间操作（lazy）：`filter/map/sorted/distinct/limit/skip`（不触发执行）
- 终止操作（触发执行）：`collect/forEach/count/reduce/anyMatch/allMatch`
- 常用收集器：`toList/toSet/toMap/groupingBy/joining/summarizingInt`
- 写 Stream 时建议：每一步表达清晰，链太长就拆变量（可读性优先）

### 3 个坑
- 忘记终止操作：只写中间操作不会执行（看不到结果）
- `toMap` key 冲突会抛异常：需要保证 key 唯一或提供合并函数
- `distinct` 依赖 `equals/hashCode`：对象不实现这两个方法时效果可能不如预期

### 1 个模板（分组统计）

```java
Map<Integer, Long> ageCount = students.stream()
    .collect(Collectors.groupingBy(Student::getAge, Collectors.counting()));
```

## Day 77（综合：把旧代码“现代化”一次）

### 5 条要点
- 用两版实现同一需求（for vs Stream）能帮你判断：哪种更清晰、更易维护
- Stream 适合“数据处理管道”：过滤/映射/分组/聚合一气呵成
- for 循环适合“复杂流程/多步状态更新”：有时更直观、调试更方便
- 边界处理很关键：空列表时平均值/最大值要安全返回
- 最终目标不是“炫 Stream”，而是“写出更容易读懂的代码”

### 3 个坑
- Stream 链太长：可读性下降，建议拆变量（每一步命名清晰）
- `max()` 返回 Optional：不要直接 `get()`，要处理空
- `toMap`/`groupingBy` 等收集器要注意 key 冲突与数据分布

### 1 个模板（for 与 Stream 对比思路）

```text
1) 先写 for 版跑通（最直观）
2) 再写 Stream 版对齐输出
3) 最后对比：可读性/边界处理/维护成本
```

