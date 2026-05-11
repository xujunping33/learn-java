# W5 Notes（每日 5 要点 + 3 个坑 + 1 个模板）

## Day 29（try-catch-finally：让程序“出错也能继续”）

### 5 条要点
- 异常是程序的“异常路径”，不是正常流程的一部分
- `try` 放可能出错的代码，`catch` 负责分类处理，`finally` 做收尾
- 多个 `catch` 要注意顺序：子类异常在前，父类异常在后
- 输入程序里用异常处理可实现“错误输入提示后重试，不直接退出”
- `finally` 在绝大多数情况下都会执行（常见例外：`System.exit`、JVM 崩溃）

### 3 个坑
- `catch (Exception)` 一把抓会掩盖细节，先写更具体的异常类型
- 忘记清理错误输入缓冲（如 `scanner.nextLine()`）会导致死循环报错
- 把业务分支全塞进 `try`，会让异常边界不清晰（只包“可能抛异常”的最小范围）

### 1 个模板（输入整数 + 异常重试）

```java
while (true) {
    try {
        return Integer.parseInt(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
        System.out.println("请输入整数。");
    } finally {
        System.out.println("本轮结束。");
    }
}
```

## Day 30（throw vs throws：谁来抛，谁来处理）

### 5 条要点
- `throw`：在方法内部主动抛出一个异常对象（立刻中断当前正常流程）
- `throws`：在方法签名声明“我可能抛这些异常”，把处理责任交给调用者
- unchecked 异常（如 `IllegalArgumentException`）通常用于参数/业务合法性校验
- checked 异常（如文件 IO）常在方法上 `throws`，由上层统一处理提示
- 设计上：底层方法专注“发现问题并抛出”，上层方法专注“统一兜底与用户提示”

### 3 个坑
- `throw` 和 `throws` 混淆：一个是语句，一个是方法签名声明
- 捕获后直接吞掉异常不处理，后续定位会非常困难
- 异常信息写太泛（如“error”），定位困难；应写清楚输入值/上下文

### 1 个模板（参数校验 + 主动 throw）

```java
public static int parsePositiveInt(String s) {
    int v = Integer.parseInt(s);
    if (v <= 0) throw new IllegalArgumentException("必须是正整数: " + v);
    return v;
}
```

## Day 31（自定义异常 + 异常链：业务错误也要讲清楚）

### 5 条要点
- 自定义异常用于表达“业务规则不满足”，比返回 `false` 更清晰
- `ValidationException` 可统一承载输入/业务校验失败信息
- 异常链（`new Xxx(msg, cause)`）可以保留底层根因，排查更高效
- `Main` 层统一 catch 并友好提示，程序可继续运行
- 业务层专注“校验并抛异常”，表现层专注“提示用户”

### 3 个坑
- 自定义异常信息太模糊（如“失败了”）会导致难定位
- catch 只打印 message 丢失 root cause，排障困难
- 所有错误都返回 boolean，调用方只能猜“为什么失败”

### 1 个模板（包装底层异常形成异常链）

```java
try {
    return Integer.parseInt(raw.trim());
} catch (NumberFormatException e) {
    throw new ValidationException("成绩解析失败: " + raw, e);
}
```

## Day 32（包装类：Integer/Double/Boolean + 装箱拆箱）

### 5 条要点
- 基本类型是值，包装类是对象；集合里存的是对象（如 `List<Integer>`）
- 自动装箱/拆箱是编译器帮你写的语法糖（`Integer x = 1; int y = x;`）
- `Integer` 的 `==` 比较的是引用，不是数值；数值比较用 `equals`
- `Integer` 在 `-128..127` 有缓存，`==` 结果会出现“看似诡异”的差异
- 拆箱前要考虑 null：`Integer n = null; int x = n;` 会抛 NPE

### 3 个坑
- 误用 `==` 比较包装类值，导致线上偶发逻辑错误
- 忘记 null 校验就拆箱，触发 `NullPointerException`
- 以为装箱/拆箱“没有成本”，在高频路径会有性能与 GC 开销

### 1 个模板（safeUnbox）

```java
public static int safeUnbox(Integer x, int defaultValue) {
    return x == null ? defaultValue : x;
}
```

## Day 33（String：常用 API + 不可变性）

### 5 条要点
- `String` 是不可变对象：任何“修改”操作都会返回新字符串
- 常用 API 要熟：`length/charAt/substring/indexOf/contains/trim/split/replace`
- 比较内容用 `equals/equalsIgnoreCase`，不要用 `==`
- 处理文本时先做“规范化”（如 `trim`、统一大小写）再做统计判断
- 在循环中频繁 `+` 拼接会产生大量临时对象（后面 Day34 用 Buffer/Builder 优化）

### 3 个坑
- 把 `trim()` 当成会原地修改：其实要接收返回值
- `split("\\s+")` 里的 `\\` 不能少（正则转义）
- 路径取文件名时要考虑“没有 `/`”的情况

### 1 个模板（压缩空白）

```java
String[] parts = s.trim().split("\\s+");
String out = String.join(" ", parts);
```

## Day 34（StringBuffer：高频拼接与线程安全）

### 5 条要点
- `StringBuffer` 是可变字符串容器，适合循环高频拼接
- `append` 支持链式调用，代码更紧凑
- `StringBuffer` 是线程安全（内部同步），通常比 `StringBuilder` 慢一些
- 单线程大多数场景优先 `StringBuilder`，多线程共享拼接再考虑 `StringBuffer`
- 图形打印可“先拼一行再输出”，结构更清晰

### 3 个坑
- 在循环里用 `String +` 容易产生大量临时对象，性能差
- 只关注“线程安全”忽略场景：单线程盲用 `StringBuffer` 可能没必要
- 拼接完成后忘记 `toString()`，拿不到最终字符串

### 1 个模板（循环拼接）

```java
StringBuffer sb = new StringBuffer();
for (int i = 1; i <= n; i++) {
    sb.append(i).append(",");
}
String out = sb.toString();
```

## Day 35（周项目：TextStats + SafeCalculatorV2）

### 5 条要点
- 异常处理让程序“面对坏输入仍可继续服务”，核心是统一捕获 + 友好提示
- `SafeCalculatorV2` 用异常处理了菜单越界、非数字输入、除 0
- `TextStats` 先做“规范化 + 统计”：字符类别统计、单词切分、最长词、高频词
- 历史记录仍然用 `ArrayList<String>`，并设置上限避免无界增长
- 项目代码要区分“正常路径”和“异常路径”，可读性会更好

### 3 个坑
- 只打印“error”不带上下文，排错很慢
- 异常被 catch 后直接吞掉，不记录/不提示，问题会隐形
- 文本统计不先 trim/split 处理空白，单词数容易算错

### 1 个模板（菜单主循环统一兜底）

```java
while (true) {
    try {
        // 读取输入 + 执行业务
    } catch (Exception e) {
        System.out.println("错误：" + e.getMessage());
    }
}
```







