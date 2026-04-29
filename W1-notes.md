# W1 Notes（每日 5 要点 + 3 个坑 + 1 个模板）

## Day 1（环境与第一个程序）

### 5 条要点
- `javac` 负责把 `.java` 编译成 `.class`；`java` 负责运行 `.class`
- Java 程序入口是 `public static void main(String[] args)`
- 类名必须和文件名一致（例如 `HelloWorld.java` 里必须是 `class HelloWorld`）
- `System.out.println(...)` 用于输出并换行；`System.out.print(...)` 不换行
- `Scanner` 可以从标准输入读取数据：`nextLine()` 读整行；`nextInt()` 读整数

### 3 个坑
- `javac` 报 “class X is public, should be declared in a file named X.java” 通常是类名/文件名不一致
- 运行时 `java` 默认从 classpath 找 `.class`，所以要么进到对应目录，要么用 `-cp` 指定
- `Scanner` 用 `nextInt()` 读数字时，输入必须是合法整数，否则会抛异常（Day6 会做安全读取）

### 1 个模板（终端编译运行）

```bash
# 在项目根目录
javac day1/*.java
java -cp day1 HelloWorld
```

## Day 2（数据类型、变量、常量、类型转换）

### 5 条要点
- `int/double/char/boolean` 是基本类型，`String` 是引用类型（对象）
- 整数除法会截断：`1/2 == 0`；想要小数结果需要至少一边是 `double`
- 强制类型转换可能导致精度丢失或溢出：例如 `(int)3.14`、`(byte)130`
- `final` 用来声明常量（约定用大写+下划线命名）
- `char` 本质是数值编码（Unicode），可以和 `int` 相互转换

### 3 个坑
- 写成 `a / b`（两个都是 `int`）时，即使赋值给 `double` 也已经截断了
- 强转不是“修复”，只是“按目标类型重新解释/截断”，需要清楚风险
- 小练习里优先用 `double` 做中间计算，最后再按需要输出/取整

### 1 个模板（把 int 除法变成小数）

```java
double result = a / (double) b;
```

## Day 3（运算符：算术/关系/逻辑/三目）

### 5 条要点
- `x++` 先“用旧值”再自增；`++x` 先自增再“用新值”
- `&&`/`||` 是短路逻辑：左边已经能决定结果时，右边不会执行
- 短路常用来避免异常：除 0、空指针、数组越界等
- 三目 `condition ? A : B` 适合做简单分支赋值（复杂逻辑还是用 if）
- 关系运算符结果一定是 `boolean`（`== != > >= < <=`）

### 3 个坑
- 把 `&` / `|` 当成 `&&` / `||`：前者不短路，右侧一定会算（容易触发异常）
- 在同一个表达式里混用多个 `++/--` 很容易读错（能拆就拆）
- `==` 比较基本类型值没问题；比较对象（如 `String`）要小心（后面会细讲）

### 1 个模板（短路避免空指针）

```java
if (s != null && s.length() > 0) {
    // safe
}
```

## Day 4（选择结构：if/else、switch）

### 5 条要点
- `if/else if/else` 适合做“范围判断”（例如分数段）
- `switch` 适合做“离散值匹配”（例如 1-7 映射星期）
- `switch` 里忘写 `break` 会 case 穿透（Java 14+ 的 `case ->` 不会穿透）
- 边界值要刻意覆盖：例如分数 0/59/60/89/90/100
- 非法输入要先拦截：非数字、越界（负数、超范围）

### 3 个坑
- 先写分支再想边界，往往漏掉 60/100 这种临界值
- `switch` 写成旧语法时漏 `break`，输出会错得离谱
- 输入不是整数时，直接 `nextInt()` 会异常；可以先 `hasNextInt()` 判断

### 1 个模板（switch 映射 1-7）

```java
switch (n) {
    case 1 -> System.out.println("星期一");
    // ...
    default -> System.out.println("非法输入");
}
```

## Day 5（循环：for/while/do-while）

### 5 条要点
- 循环三要素：初始化（init）/ 条件（condition）/ 迭代（update）
- `for` 更适合“次数明确”的循环（如 1..n 求和、乘法表）
- `while` 更适合“次数不确定”的循环（如不断输入直到 0 结束）
- 写循环先想“终止条件”，再写循环体，最后检查边界（0/1/负数）
- 尽量少用 `break/continue`，先把逻辑写清楚（必要时再用）

### 3 个坑
- 条件写错：`i < n` 和 `i <= n` 差一个边界值
- 阶乘等计算容易溢出：类型要选对（这里用 `long` 并限制 n<=20）
- 统计输入题：忘记处理非法输入或忘记更新计数器/迭代变量会死循环

### 1 个模板（欧几里得算法求 gcd）

```java
while (b != 0) {
    long t = a % b;
    a = b;
    b = t;
}
```

## Day 6（Debug 实战 + 输入校验）

### 5 条要点
- 断点调试最常用：打断点 → Step Over（逐行）→ 观察变量 → 找到分支/循环为什么走错
- `Scanner.nextInt()`/`nextDouble()` 读到非法输入会抛异常；要么先 `hasNextInt()`，要么统一用 `nextLine()` 再解析
- “吞换行”常见场景：先 `nextInt()` 再 `nextLine()`，中间遗留的换行会被 `nextLine()` 直接读走（看起来像跳过输入）
- 一个稳妥策略：**统一用 `nextLine()` 读整行，再 `parseInt/parseDouble`**
- 输入校验要做两层：格式校验（是不是数字）+ 范围校验（是否在 min..max）

### 3 个坑
- 只做范围校验不做格式校验：输入 `abc` 就崩
- 修“吞换行”只靠多读一次 `nextLine()` 容易写乱；不如统一行读取策略
- Debug 时只看输出不看变量变化，很难定位“条件为什么一直为 true/false”

### 1 个模板（安全读 int：nextLine + parse）

```java
while (true) {
    String line = scanner.nextLine().trim();
    try {
        int v = Integer.parseInt(line);
        return v;
    } catch (NumberFormatException e) {
        System.out.println("请输入整数。");
    }
}
```

## Day 7（周项目：MenuCalculator + 周复盘）

### 5 条要点
- 菜单循环本质：`while(true)` + `switch(choice)` + `0` 退出
- 输入校验要统一：菜单用范围校验，数字用 parse 校验，避免异常导致崩溃
- 运算逻辑与交互分离：`Main` 管流程，`Calculator` 管计算，`SafeInput` 管输入
- 除法必须处理除 0：给出提示并不中断程序
- 历史记录用 `ArrayList<String>`，并限制最大条数（超过就删最早的一条）

### 3 个坑
- 菜单项越界/输入字母：如果不拦截，程序要么崩要么进入异常分支
- 历史记录无限增长：用上限控制（如 20 条）
- 把所有逻辑堆在 `main`：后期改需求会非常痛苦（难读、难测、难调试）

### 1 个模板（历史记录限制 20 条）

```java
history.add(entry);
if (history.size() > 20) history.remove(0);
```

