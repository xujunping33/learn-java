## 第11周学习计划：Java高级编程（Maven / 工厂模式 / 反射 / Lambda / Stream）

对应原路径：第11周《Java高级编程》。  
学习时长：每天约 2 小时。

本周核心目标
- **Maven**：会创建 Maven 项目、会用常用命令、理解依赖与生命周期；从本周开始为后续项目固定“工程化骨架”。
- **设计模式（工厂）**：理解简单工厂/工厂方法的动机与使用场景。
- **反射**：理解 `Class/Field/Method/Constructor` 的基本用法，知道框架为什么离不开反射。
- **Java 8（Lambda/Stream）**：能写常见 Lambda、会方法引用；Stream 能完成常见集合处理（过滤/映射/分组/排序/聚合）。

本周交付物（必须完成）
- `day71`：`maven-demo/`（一个能跑的 Maven 项目，含 main 方法）
- `day72`：`maven-demo` 增加 JUnit5 单测（至少 3 个）
- `day73`：`FactoryDemo`（简单工厂 + 工厂方法各 1 个例子）
- `day74`：`ReflectionDemo`（反射创建对象 + 调用方法 + 读取/修改字段各 1 次）
- `day75`：`LambdaMethodRefDemo`（Lambda + 方法引用 4 种：静态/实例/对象/构造器）
- `day76`：`StreamApiDemo`（创建流 + 中间操作 + 终止操作，覆盖 10 个常用场景）
- `day77`：综合小作业：把你 `Student` 列表统计（平均/最高/分组）改成 Stream 版 + 对比可读性
- `W11-notes.md`

目录建议
- `day71` ~ `day77`
- 本周建议开始引入 `maven-demo/` 作为“工程化模板”，后面 JDBC/MyBatis/Spring 都会受益

每天固定节奏（2小时）
- 20min：复盘（重跑昨日 demo，补 1 个边界用例）
- 70min：主编码任务（必须可运行/可测试）
- 20min：总结入 `W11-notes.md`
- 10min：口述复盘（用自己的话解释：Maven/反射/Stream 各解决什么问题）

---

## Day 71（Maven 入门：创建项目 + 生命周期）

学习要点
- Maven 的价值：依赖管理、统一构建、可复现
- 生命周期：`clean`、`compile`、`test`、`package`
- 目录结构：`src/main/java`、`src/test/java`

任务卡（70min）
- 创建 `maven-demo`（可以用 IDE 创建或手动）
- 写一个 `App` main 方法输出 “maven ok”
- 跑通命令
  - `mvn -v`
  - `mvn clean compile`
  - `mvn -q exec:java`（可选；不会也没关系）

验收标准
- 你能解释：为什么 Maven 项目结构固定且重要

---

## Day 72（Maven 依赖 + JUnit5 单测）

学习要点
- `pom.xml`：`groupId/artifactId/version/dependencies`
- 单测：让你“改代码不心虚”

任务卡（70min）
- 给 `maven-demo` 加 JUnit5 依赖
- 写 3 个单测（建议：字符串处理、计算、边界值）
- 跑通 `mvn test`

验收标准
- 你能做到：单测失败时能定位失败原因并修复

---

## Day 73（工厂模式：简单工厂 vs 工厂方法）

学习要点
- 动机：消除 new 的分散、让创建逻辑集中
- 简单工厂：一个工厂按参数返回不同实现
- 工厂方法：不同工厂负责创建不同产品

任务卡（70min）
- 做一个 `Payment` 或 `Shape` 的工厂案例
  - 简单工厂：`PaymentFactory.create(type)`
  - 工厂方法：`AliPayFactory.create()` / `WeChatPayFactory.create()`

验收标准
- 你能解释：为什么“新增一种类型”时，工厂能降低修改范围

---

## Day 74（反射：Class/Field/Method/Constructor）

学习要点
- `Class.forName` / `obj.getClass`
- 通过 `Constructor` 创建对象
- 通过 `Method` 调用方法
- 通过 `Field` 读取/修改字段（`setAccessible(true)` 的意义）

任务卡（70min）
- 写 `ReflectionDemo`
  - 反射创建 `Student`
  - 反射调用 `setScore/getScore`
  - 反射读取并修改一个 private 字段（只做演示）

验收标准
- 你能口述：为什么框架能“没有 new 也能创建对象”（IoC/反射直觉）

---

## Day 75（Lambda + 方法引用）

学习要点
- Lambda：把函数当参数传
- 方法引用 4 种
  - 静态方法引用：`ClassName::staticMethod`
  - 特定对象实例方法：`obj::method`
  - 特定类型任意对象实例方法：`ClassName::method`
  - 构造器引用：`ClassName::new`

任务卡（70min）
- 写 `LambdaMethodRefDemo`
  - 用 `Comparator` 排序学生
  - 用方法引用改写 Lambda

验收标准
- 你能解释：什么时候 Lambda 更清晰，什么时候方法引用更清晰

---

## Day 76（Stream API：创建/中间/终止）

学习要点
- 创建：`stream()`、`Arrays.stream()`、`Stream.of()`
- 中间：`filter/map/sorted/distinct/limit/skip`
- 终止：`collect/forEach/count/reduce/anyMatch/allMatch`
- 常用收集：`Collectors.toList/toMap/groupingBy/joining/summarizingInt`

任务卡（70min）
- 写 `StreamApiDemo`（建议用 `List<Student>` 作为数据源）
  - 过滤不及格
  - 找最高分
  - 按年龄分组统计人数
  - 按分数排序取 Top3
  - 转成 `Map<id,Student>`

验收标准
- 你能读懂一段常见 Stream 链并解释每一步在做什么

---

## Day 77（综合：把旧代码“现代化”一次）

任务卡（70min）
- 选你之前的一个统计模块（例如学生统计/员工统计）
- 写两版
  - 传统 for 循环版
  - Stream 版
- 对比：可读性、可维护性、边界处理（空列表）

验收标准（完成即过关）
- 你能口述：为什么 Stream 适合“数据处理管道”，但有时 for 更直观

