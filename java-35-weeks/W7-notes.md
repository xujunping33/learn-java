# W7 Notes（每日 5 要点 + 3 个坑 + 1 个模板）

## Day 43（线程间通信：wait/notify）

### 5 条要点
- `wait()`：释放锁并进入等待状态，直到被 `notify/notifyAll` 唤醒
- `notify()` / `notifyAll()`：唤醒等待线程（通常配合 `while` 条件判断）
- 正确写法：**条件不满足就 wait，条件满足后再继续**
- 生产者/消费者：共享一个缓冲区（这里是有界队列）
- 避免死等：生产/消费数量对齐，或者用明确结束条件

### 3 个坑
- 不用 `while` 包裹 `wait()`：容易因为虚假唤醒/时序问题导致错误消费/生产
- 忘记在同步块里调用 `wait/notify`：会抛 `IllegalMonitorStateException`
- 写了 `notify` 但可能唤错类型线程（生产者/消费者），建议用 `notifyAll` 简化教学

### 1 个模板（wait/notifyAll + while 条件）

```java
synchronized (lock) {
    while (条件不满足) {
        lock.wait();
    }
    // 条件满足后执行操作
    lock.notifyAll();
}
```

## Day 44（线程同步：StopFlag 标志位 + 可见性）

### 5 条要点
- 多线程里“一个线程把 flag 改为 false”，另一个线程未必立刻能看到：这是**可见性问题**
- `volatile` 能让标志位的读写对所有线程可见（最基础的可见性保障）
- 标志位退出循环的常见写法：`while (running) { ... }`
- `volatile` 不是互斥锁：它不保证复合操作原子性
- `join()` 用于等待线程真正结束，方便你验证效果

### 3 个坑
- 不加 `volatile` 的 stop flag 可能退出得很慢，甚至看起来“停不下来”（取决于 JVM/JIT/CPU）
- 误以为 `volatile` 等于 `synchronized`（后者还有互斥/临界区语义）
- 忘记处理中断：如果用 `interrupt()` 做兜底，循环里要响应中断

### 1 个模板（volatile stop flag）

```java
private volatile boolean running = true;

public void stop() {
    running = false;
}

public void run() {
    while (running) {
        // work
    }
}
```

## Day 45（I/O强化：BinaryCopyDemo）

### 5 条要点
- 字节流复制文件使用 `FileInputStream/FileOutputStream`
- 不加 buffer：一次 `read()`/`write()` 一个字节，会导致系统调用次数非常多
- 加 buffer：用 `byte[] buf` 批量读写，显著减少系统调用
- 用 `try-with-resources` 确保流一定关闭
- 验收：复制后文件大小一致（可选再做内容校验）

### 3 个坑
- 无 buffer 在大文件上会非常慢
- 把字符流用于二进制数据会导致乱码或内容损坏
- 忘记处理 `read()` 返回 `-1`（会出现死循环或漏数据）

### 1 个模板（带 buffer 的字节拷贝）

```java
try (InputStream in = new FileInputStream(src);
     OutputStream out = new FileOutputStream(dst)) {
    byte[] buf = new byte[8192];
    int len;
    while ((len = in.read(buf)) != -1) {
        out.write(buf, 0, len);
    }
}
```

## Day 46（I/O与并发结合：RandomAccessFile 分块写）

### 5 条要点
- `RandomAccessFile` 可以 `seek(pos)` 指定读写位置，适合并发分块写入
- 分块核心是计算每个线程的 `[start, end)`，确保**无重叠、无遗漏**
- 目标文件最好先 `setLength(fileSize)` 预分配长度
- 每个线程只处理自己 chunk 的范围，互不干扰
- 主线程 `join` 等全部线程完成，再做校验（至少大小一致）

### 3 个坑
- 分块边界写错（`end` 是否包含）会导致重复/丢失字节
- 多线程写同一文件但不控制写入位置，会互相覆盖
- 忘记预分配长度，可能导致部分位置写入异常或文件尺寸不对

### 1 个模板（chunk 计算：ceil 切分）

```java
long chunkSize = (size + threadCount - 1) / threadCount;
for (long start = 0; start < size; start += chunkSize) {
    long end = Math.min(start + chunkSize, size);
    // chunk = [start, end)
}
```

## Day 47（综合案例核心：MultiThreadFileDownloader）

### 5 条要点
- 输入：源路径、目标路径、线程数（可默认）
- 分块：按 size 切 `[start,end)`，每个线程负责一段
- 写入：用 `RandomAccessFile` + `seek(start)` 并发写目标文件不同位置
- 等待：主线程对所有 worker `join()`，确保全部完成
- 校验：至少对比源/目标文件大小一致（后续可加 hash）

### 3 个坑
- 目标文件不预分配长度，可能导致写入不完整或尺寸异常
- chunk 边界计算错误会造成覆盖/遗漏
- 线程内异常不会自动让主线程失败（Day48 会增强汇报）

### 1 个模板（主线程等待全部完成）

```java
for (Thread t : threads) t.start();
for (Thread t : threads) t.join();
```

## Day 48（增强：异常处理与进度统计）

### 5 条要点
- 子线程异常不会自动让主线程失败：需要“捕获并汇报”
- 常见做法：用共享变量（如 `AtomicReference<Throwable>`）保存第一个错误
- 进度统计可用 `AtomicLong doneBytes` 汇总已完成字节
- 进度线程/定时打印：每隔一段时间输出百分比
- 一旦出现错误，其他线程应尽快停止（检查错误标志）

### 3 个坑
- 线程里 catch 了异常只打印，不告诉主线程：主线程会误以为成功
- 进度统计要考虑线程安全（用 AtomicLong 或同步）
- 频繁打印进度会影响性能（粗粒度即可）

### 1 个模板（子线程错误汇报）

```java
AtomicReference<Throwable> firstError = new AtomicReference<>(null);
try {
    // work
} catch (Throwable e) {
    firstError.compareAndSet(null, e);
}
```

## Day 49（收尾：稳定性验证 + 周复盘）

### 线程间通信解决了什么问题？
- 解决“生产者太快/消费者太慢”或“消费者太快/生产者太慢”的协调问题
- 通过共享缓冲区 + 条件 + `wait/notifyAll`，让线程在**条件不满足时等待**，满足时继续

### 并发分块写解决了什么问题？
- 解决“大文件拷贝/下载速度慢”的问题：把任务分成多个 chunk 并行处理
- 用 `RandomAccessFile.seek(start)` 保证每个线程只写自己的文件区间，避免互相覆盖
- 用 `join()` 保证主线程在所有分块完成后再校验/收尾

### 本周最常用的 3 个模板
- `while + wait/notifyAll` 的条件等待模板
- `chunk` 计算模板（ceil 切分 `[start,end)`）
- `join` 等待模板（所有线程结束再合并/校验）

