## 第5周：异常、包装类与字符串（按35周路径版）

学习目标
- 异常：会用 `try-catch-finally` 让程序“出错也不中断”；理解 `throw`/`throws`；会写自定义异常与异常链。
- 包装类：理解装箱/拆箱、`Integer` 比较与 `null` 拆箱风险。
- 字符串：熟练 `String` 常用 API；会用 **`StringBuffer`** 做拼接（并理解它与 `StringBuilder` 的区别）。

时间投入：每天约 2 小时

本周交付物（必须做到）
- **异常综合练习**：`SafeCalculatorV2`（异常驱动的输入校验与友好提示，不因异常退出）
- **自定义异常与异常链**：`ValidationException`（或 `BusinessException`）+ 至少 1 个“包装底层异常”的例子
- **字符串小项目**：`TextStats`（文本统计：字符/数字/空白/单词/最长单词/高频词）
- **笔记**：`W5-notes.md`

目录建议：`day29` ~ `day35`

每天固定节奏（2小时）
- 20min：复盘昨天（把昨天程序再跑一遍，补 1 个边界用例）
- 70min：写代码（必须能编译运行）
- 20min：总结写入 `W5-notes.md`
- 10min：口述复盘（异常与字符串：各用 3 句话讲清楚）

---

## Day 29（try-catch-finally：让程序“出错也能继续”）

学习要点
- 异常是什么：程序的“异常路径”
- `try-catch-finally`：捕获、处理、收尾（资源释放）
- 多 catch、catch 顺序（子类在前，父类在后）

任务卡（70min编码）
- `TryCatchDemo`
  - 捕获 `ArithmeticException`（除 0）
  - 捕获 `InputMismatchException`（输入非数字）
  - finally 中输出“本轮结束/资源关闭提示”
- 把你 W1/W2 的任意一个输入程序改成：错误输入不会直接结束，而是提示重输

验收标准
- 你能解释：finally 什么时候一定执行、什么时候可能不执行（先掌握“几乎总会执行”并知道 `System.exit` 例外即可）

---

## Day 30（throw vs throws：谁来抛，谁来处理）

学习要点
- `throw`：在方法内部“主动抛出”
- `throws`：在方法签名上“声明可能抛出”
- checked vs unchecked（先建立概念：编译期要求处理的 vs 不强制）

任务卡（70min编码）
- `ThrowThrowsDemo`
  - 写 `parsePositiveInt(String s)`：不合法就 `throw new IllegalArgumentException(...)`
  - 写 `readFileFirstLine(String path)`：先声明 `throws Exception`（或更具体异常），在调用处捕获并打印友好提示

验收标准
- 你能说清楚：什么时候“就地 try-catch”，什么时候“往上 throws”

---

## Day 31（自定义异常 + 异常链：业务错误也要讲清楚）

学习要点
- 自定义异常：表达“业务不满足”而不是系统崩溃
- 异常链：`new XException(msg, cause)` 保留根因

任务卡（70min编码）
- 自定义异常（二选一）
  - `ValidationException extends RuntimeException`
  - `BusinessException extends RuntimeException`
- 写 `StudentServiceV2`（可复用你 day14/day18 的逻辑）
  - id 重复、score 越界等不再返回 boolean，而是抛自定义异常
  - `Main` 统一 catch 并打印友好提示（不中断程序）
- 写一个异常链示例：底层捕获后包装成业务异常抛出

验收标准
- 你能解释：为什么“返回 false”不如“抛明确异常”可维护（尤其是错误原因多的时候）

---

## Day 32（包装类：Integer/Double/Boolean + 装箱拆箱）

学习要点
- 基本类型 vs 包装类（集合只能放对象）
- 自动装箱/拆箱（编译器帮你做，但有坑）
- `Integer` 缓存与 `==`（重要：对象比较用 `equals`）

任务卡（70min编码）
- `WrapperDemo`
  - 演示装箱/拆箱
  - 演示 `Integer a=127; Integer b=127; a==b` 与 `128` 的差异（写结论到笔记）
  - 演示 `null` 拆箱触发 `NullPointerException`
- 练习：写 `safeUnbox(Integer x, int defaultValue)`：null 时返回默认值

验收标准
- 你能说清楚：为什么包装类比较要用 `equals`，以及 `null` 拆箱为什么会 NPE

---

## Day 33（String：常用 API + 不可变性）

学习要点
- String 不可变：拼接会产生新对象
- 常用 API：`length/charAt/substring/indexOf/contains/startsWith/endsWith/trim/split/replace`
- `equalsIgnoreCase`

任务卡（70min编码）
- `StringApiPractice`：完成至少 10 个小任务
  - 判断回文
  - 统计某个字符出现次数
  - 去掉字符串两端空格并压缩中间多空格（可先做简版）
  - 邮箱/手机号格式（先做最简规则，不强求正则）
  - 从路径里取文件名（例如 `/a/b/c.txt` -> `c.txt`）

验收标准
- 你能解释：为什么在循环里用 `+` 拼接会慢

---

## Day 34（StringBuffer：高频拼接与线程安全）

学习要点
- `StringBuffer` 的使用场景：循环拼接、构建大文本（线程安全）
- `append` 链式写法
- `StringBuffer` vs `StringBuilder`
  - **相同点**：都可变，适合循环拼接
  - **不同点**：`StringBuffer` 线程安全（同步），通常更慢；单线程大多用 `StringBuilder`

任务卡（70min编码）
- `BufferDemo`
  - 用 String 在循环拼接 1..n 输出（体验“写起来简单但不适合大循环”）
  - 用 `StringBuffer` 重写一遍
- 把你 Day9 的图形打印（星号）用 `StringBuffer` 拼一行再输出（更干净）

验收标准
- 你能说清楚：什么时候用 String，什么时候用 StringBuffer
- 并且你知道：大多数单线程场景为什么更推荐 `StringBuilder`（写到笔记里即可）

---

## Day 35（周项目：TextStats + SafeCalculatorV2）

项目 1：TextStats（必须）
- 输入一段文本（多行也行，先做单行版即可）
- 输出统计
  - 字符总数
  - 字母数/数字数/空白数
  - 单词数（按空白切分，先做简版）
  - 最长单词
  - Top3 高频单词（可选加练，允许只做 Top1）

项目 2：SafeCalculatorV2（必须）
- 菜单循环（加减乘除、历史、退出）
- 所有错误都用“异常 + 友好提示”处理
  - 非数字输入
  - 除 0
  - 菜单越界
- 历史记录用 `ArrayList<String>`

验收标准（完成即过关）
- 乱输字符、除 0、空输入，程序都不会直接崩溃退出
- 你能用 90 秒讲清楚：异常在这个程序里解决了什么问题

本周复盘（30min）
- 写下你最常见的 5 个异常来源（输入、数组越界、空指针、类型转换、文件 I/O）
- 写 3 个你自己的“异常处理模板”（统一捕获/统一提示/必要时抛出业务异常）

