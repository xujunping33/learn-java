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

