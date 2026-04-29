## W3 学习计划：面向对象（封装与继承）

学习目标：真正把“面向对象”用起来：会设计类、写构造器、用封装保护数据；会用包组织代码；理解并应用继承/方法重写/`super`；掌握 `static` 变量与方法的使用边界。

时间投入：每天约 2 小时

本周交付物（必须做到）
- **封装综合案例升级**：把 `day14` 的学生管理升级为“包结构 + 更严格封装 + 更清晰的职责拆分”
- **大作业**：`DepartmentEmployeeManager`（部门员工管理：继承 + 重写 + super + static 统计）
- **笔记**：`W3-notes.md`（每天 5 条要点 + 3 个坑 + 1 个模板）

目录建议
- `day15` ~ `day21`

每天固定节奏（2小时）
- 20min：复盘昨天 + 读一遍昨天的核心类（大声解释字段/方法的职责）
- 70min：写代码（必须能编译运行）
- 20min：总结写入 `W3-notes.md`
- 10min：口述复盘（面试训练：封装/继承/重写/静态 的一句话解释）

---

## Day 15（类与对象：从“会用”到“会设计”）

学习要点
- 类 vs 对象
- 字段/方法的职责边界（不要把所有逻辑都塞进 `Main`）
- `toString()` 的意义（用于调试/打印）

任务卡（70min编码）
- 在 `day15` 写 `Book` 类（`id/title/author/price`）
  - 能创建对象、修改字段（先不封装）、打印对象
- 写 `BookDemo`：创建 3 本书，输出最贵的一本

验收标准
- 你能讲清楚：为什么要把数据和行为放进类里，而不是全写在 `main`

---

## Day 16（构造方法：初始化策略 + 重载）

学习要点
- 构造器用于“保证对象从出生就合法”（初始化约束）
- 构造器重载：默认值、不同初始化方式
- `this(...)` 调用同类构造器（减少重复）

任务卡（70min编码）
- `Student` 练习（独立于 day14 的版本也可以）
  - 字段：`id/name/score`
  - 至少 2 个构造器：`(id,name)`、`(id,name,score)`
  - `score` 默认 0（或 -1 表示未录入，二选一并写到笔记里）
- `ConstructorDemo`：验证不同构造器创建出来的对象状态

验收标准
- 你能解释清楚：为什么“构造器里做合法性检查”比创建后再检查更可靠

---

## Day 17（封装：private + getter/setter + 校验）

学习要点
- `private` 的意义：不让外部随便改，类自己负责维护不变量
- setter 校验：把“合法性规则”收拢到一个地方
- 不要滥用 setter：能只读就只读（只给 getter）

任务卡（70min编码）
- 重写一个 `Person` 类（`id/name/age`）
  - `age` 只能 0–150，否则拒绝设置
  - `name` 不能为空
- 写 `EncapsulationDemo`：尝试设置非法值，验证保护生效

验收标准
- 你能说清楚：封装的核心是“控制修改入口”，不是“为了写 getter/setter”

---

## Day 18（包 package：让代码规模化）

学习要点
- 包名规范：`com.xxx.project.module`
- 同包访问、跨包 import
- 目录结构与包声明必须一致

任务卡（70min编码）
- 把 `day14` 的学生管理复制/迁移到 `day18`，并拆成包（示例）
  - `com.xjp.sms.model`：`Student`
  - `com.xjp.sms.service`：`StudentService`
  - `com.xjp.sms.util`：`SafeInput`
  - `com.xjp.sms.app`：`Main`
- 确保能在终端编译运行（你会因此更理解“包”的本质）

验收标准
- 你能在笔记里写下：为什么“文件夹名”和 `package` 必须对应

---

## Day 19（static：类变量/类方法的边界）

学习要点
- `static` 属于类，不属于对象
- 适合：常量、工具方法、全局计数器（谨慎）
- 不适合：表示对象独有状态的数据

任务卡（70min编码）
- 在 `Student` 或 `Employee` 中加 `static int count`（创建对象时计数）
- 写 `IdGenerator`（静态方法生成递增 id，先做最简单版即可）
- 写 `StaticDemo` 验证：不同对象共享同一个 static 数据

验收标准
- 你能解释清楚：为什么 `static` 在多线程/长期运行程序里要更谨慎（先讲“共享”即可）

---

## Day 20（继承：is-a 关系 + 方法重写）

学习要点
- 继承表达 is-a：`Employee` 是一个 `Person`
- 方法重写：同名同参同返回（或协变），运行时多态（先理解“子类覆盖父类实现”）
- `@Override` 的价值：防止写错签名导致“没有真的重写”

任务卡（70min编码）
- 设计类层次（示例）
  - `Person`：`id/name`
  - `Employee extends Person`：`baseSalary`
  - `Manager extends Employee`：`bonus`
- 重写 `toString()`（至少在 `Employee/Manager` 中）
- 写 `InheritanceDemo`：创建 `Employee` 与 `Manager`，用同一打印方法输出

验收标准
- 你能说清楚：什么时候适合继承，什么时候更适合组合（先举例即可）

---

## Day 21（super：复用父类逻辑 + 大作业：部门员工管理）

学习要点
- `super(...)` 调父类构造器（初始化父类部分）
- `super.method()` 调父类实现（在子类增强而不是完全重写时很有用）
- 继承与封装一起用：父类字段也应 `private`，通过受控方法访问

大作业：DepartmentEmployeeManager（必须实现第一版）
- 业务需求（控制台）
  - 新增员工（普通员工/经理）
  - 删除员工
  - 查询员工（按 id）
  - 列表展示
  - 计算工资（普通员工=base；经理=base+bonus）
- 类设计建议（可调整，但要有继承）
  - `Person`（父类）
  - `Employee`（子类）
  - `Manager`（子类）
  - `DepartmentService`（增删改查 + 统计）
  - `Main`（菜单）
- 必须点
  - 经理工资计算必须体现“重写/扩展”
  - 至少一处使用 `super(...)` 或 `super.xxx()`（写到代码里，不只是理解）

验收标准（完成即过关）
- 程序能运行 10 分钟不崩溃
- 你能用 90 秒讲清楚：类层次怎么设计的、为什么这么设计、重写发生在哪里

