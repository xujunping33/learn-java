# W6 Notes（每日 5 要点 + 3 个坑 + 1 个模板）

## Day 36（List / Set：数据容器升级）

### 5 条要点
- `ArrayList` 适合有序、可重复、按索引访问的数据
- `HashSet` 适合去重（无序、不重复）
- 集合比数组更适合“动态数量”数据（不用手动扩容）
- `List` 常用操作：`add/remove/set/get/contains/isEmpty`
- 先用 `List` 管理原始数据，再按需转 `Set` 做去重是常见模式

### 3 个坑
- 边遍历边按索引删除容易跳元素（要小心迭代方式）
- 误以为 `HashSet` 保持插入顺序（它不保证顺序）
- `List` 下标越界会抛异常，先判断 `size` 再 `get/set`

### 1 个模板（List 去重到 Set）

```java
ArrayList<String> list = new ArrayList<>();
// ... add
HashSet<String> set = new HashSet<>(list);
```

## Day 37（Map：键值数据管理）

### 5 条要点
- `HashMap<K,V>` 适合“按 key 快速查找”的场景（如 id -> 学生）
- `containsKey` 用于判断 key 是否存在，避免重复插入
- 常见遍历有 `entrySet`（拿 key+value）和 `keySet`（先 key 再 get）
- `map.values()` 适合做统计（平均分/最高分/最低分）
- 对“按 id 查对象”这种需求，`Map` 比 `List` 更直接，通常更高效

### 3 个坑
- `map.get(key)` 可能返回 null，直接用其成员会 NPE
- 遍历时修改 map 结构会触发并发修改问题（后续专门讲）
- 用错 key 类型或 key 规则不稳定会导致“查不到本该能查到的数据”

### 1 个模板（按 id 查询并判空）

```java
Student s = map.get(id);
if (s == null) {
    System.out.println("未找到");
} else {
    System.out.println(s);
}
```

## Day 38（泛型基础：泛型类 + 泛型方法）

### 5 条要点
- 泛型用于在编译期保证类型安全，减少强制类型转换
- `Box<T>` 这类泛型类：`T` 会在创建对象时具体化为真实类型
- 泛型方法：`public static <T> ...`，让方法对类型也“参数化”
- `List<T>` 等集合里，`T` 决定你能放什么类型、返回什么类型
- 返回值用泛型能减少 `Object` 再强转的问题

### 3 个坑
- 忘了写 `<T>`（泛型声明）会导致编译不过
- 在泛型里用原生类型（raw type）会触发 warning（不推荐）
- 泛型不能对 `T` 做具体类型的 `new T()`（需要额外信息/反射才行）

### 1 个模板（泛型方法签名）

```java
public static <T> T firstOrNull(List<T> list) {
    if (list == null || list.isEmpty()) return null;
    return list.get(0);
}
```

## Day 39（线程创建与生命周期）

### 5 条要点
- 两种创建方式：继承 `Thread` 或实现 `Runnable`
- `start()` 才会真正启动线程并进入就绪/运行；直接 `run()` 只是普通方法调用
- 生命周期常见状态：`NEW -> RUNNABLE -> TIMED_WAITING/WAITING -> TERMINATED`
- `sleep` 会让线程进入等待（不释放锁的语义取决于场景，但本课程先记“暂停”即可）
- `join` 用于让主线程等待子线程结束

### 3 个坑
- 误用 `run()` 代替 `start()`：不会并发
- 忘记 `join()` 导致主线程先结束，观察到输出顺序混乱
- `InterruptedException` 捕获后不处理：应该恢复中断标记或决定退出

### 1 个模板（主线程等待子线程）

```java
try {
    t.join();
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

## Day 40（线程同步与线程安全：多线程卖票）

### 5 条要点
- 多线程共享可变数据时会出现并发问题（即使代码看起来“简单”）
- 卖票本质是“读 -> 判断 -> 写”的临界区（需要原子性）
- `synchronized` 会让同一把锁下的代码段一次只能被一个线程执行
- 不加锁版本会产生竞态：多个线程基于旧值同时卖票
- 加锁版本保证“票数检查与扣减”在同一临界区完成

### 3 个坑
- 以为 `tickets--` / `++` 是原子的：实际上是多个步骤，不是原子操作
- 锁用错对象（每个线程用不同锁）会导致锁失效
- 把耗时操作放在锁内导致性能下降（本例为了观察现象，故意在锁内 sleep）

### 1 个模板（同步代码块）

```java
synchronized (lock) {
    if (tickets <= 0) return;
    tickets--;
}
```

## Day 42（周整合：集合 + 泛型 + 线程 + I/O）

### 5 条要点
- I/O：用字符流读文件文本（读取日志内容）
- 集合/Map：用 `Map<String,Integer>` 统计 token 出现次数
- 多线程：把 token 按区间分段给多个线程，线程内做局部统计
- 合并策略：所有线程结束后，在主线程合并局部 Map，避免复杂同步
- 泛型：`List<String>`、`Map<String,Integer>` 的类型安全减少强转

### 3 个坑
- 线程内直接写同一个全局 `Map` 会带来同步开销或并发错误
- token 切分规则不一致会导致统计结果“看起来不对”
- 忘记 `join`：主线程可能在子线程统计未完成前就开始合并

### 1 个模板（线程分段 + join 合并）

```java
// 1) 切分区间给每个 Worker
// 2) start()
for (Thread t : threads) t.start();

// 3) 等所有线程结束
for (Thread t : threads) t.join();

// 4) 合并各自 local Map 到 global Map
for (Worker w : workers) merge(global, w.local);
```


## Day 41（I/O基础：字节流与字符流）

### 5 条要点
- 字节流面向字节：`InputStream/OutputStream`（适合拷贝文件、二进制）
- 字符流面向字符：`Reader/Writer`（适合文本：编码下更直观）
- `try-with-resources` 自动关闭资源，避免流泄漏
- 字符流常用：`FileReader/FileWriter` 配合 `BufferedReader/BufferedWriter`
- 字节流拷贝：用缓冲区循环读写，处理任意大小文件（这里演示 txt）

### 3 个坑
- 只用一个循环却不读到 `-1`：容易死循环或漏数据
- 读写用错类型（文本用字节、二进制用字符）会出现乱码/错误
- 关闭不当：忘记关闭会导致内容不落盘或文件句柄泄漏

### 1 个模板（字节流拷贝）

```java
try (InputStream in = new FileInputStream(src);
     OutputStream out = new FileOutputStream(dst)) {
    byte[] buf = new byte[4096];
    int len;
    while ((len = in.read(buf)) != -1) {
        out.write(buf, 0, len);
    }
}
```




