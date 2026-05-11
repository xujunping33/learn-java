## 第7周学习计划：线程同步/通信 + 多线程下载器 + I/O（下半段）

对应原路径：第6~7周《集合、泛型、多线程、I/O与综合案例》中“线程同步与通信 + 综合案例收尾（多线程下载器）”的部分。

学习时长：每天约 2 小时

本周目标
- 线程同步：会用 `synchronized` + `wait/notify/notifyAll` 解决“线程间通信”
- I/O：会用字节流进行文件/网络数据传输的分块写入（理解关键 API）
- 综合案例：完成一个“多线程下载器/多线程文件拷贝器”（能稳定运行、输出进度）

本周交付物（必须完成）
- `ThreadCommunicationDemo`：生产者-消费者模型（wait/notify）
- `MultiThreadFileDownloader`：多线程分块下载/拷贝（支持 join + 结果校验）
- `W7-notes.md`：每天5条要点 + 3个坑 + 1个模板

目录建议
- `day43` ~ `day49`

每天固定节奏（2小时）
- 20min：复盘昨天并重跑（修1个边界问题）
- 70min：主编码任务（必须可编译运行）
- 20min：总结入 `W7-notes.md`
- 10min：口述复盘（写清你今天用到的同步/通信方式）

---

## Day 43（线程间通信：wait/notify）

学习要点
- `wait()`：释放锁并进入等待
- `notify/notifyAll()`：唤醒等待线程（通常配合 while 条件判断）
- 生产者-消费者的正确写法：共享队列 + 条件 + 循环

任务卡（70min）
- 写 `ThreadCommunicationDemo`
  - 共享一个 `Queue<Integer>` 或自定义 `BoundedBuffer`
  - 生产者：持续生产随机整数并放入缓冲区（上限比如 10）
  - 消费者：持续消费并打印（空了就等待）
  - 运行结束条件：生产固定数量 N（例如 30），消费也对应 N

验收标准
- 程序不会死锁/不会无限等待
- 你能说清楚：为什么要用 `while` 包裹 `wait()`（防止虚假唤醒/条件不满足）

---

## Day 44（线程同步：更稳的临界区与可见性）

学习要点
- 临界区：共享数据的访问必须在同一把锁内
- `volatile` 的直觉：让其它线程可见（只做最基础使用）

任务卡（70min）
- 写 `StopFlagDemo`
  - 用一个 `volatile boolean running` 控制线程循环退出
  - 再写一个对比版本：不加 volatile（只做认识，不强制必现问题）

验收标准
- 你能解释：为什么“标志位”在多线程下需要可见性保障

---

## Day 45（I/O强化：字节流分块读写 + 缓冲）

学习要点
- `FileInputStream/FileOutputStream`：字节级读写
- `BufferedInputStream/BufferedOutputStream`：减少系统调用
- `try-with-resources` 保证关闭

任务卡（70min）
- 写 `BinaryCopyDemo`
  - 把一个本地文件复制到另一个文件
  - 分别实现：不加 buffer 与加 buffer（结构可对比即可）
  - 输出：复制耗时（粗略即可）

验收标准
- 复制后文件大小一致；必要时做简单校验（例如字节数/或 hash 先不强求）

---

## Day 46（I/O与并发结合：RandomAccessFile 分块写）

学习要点
- `RandomAccessFile`：允许指定位置写入（适合并发分块）
- 分块思路：根据文件大小计算每个线程的起止偏移

任务卡（70min）
- 写 `ChunkedCopySupport`
  - 工具方法：根据 `fileSize`、`threadCount` 计算每个 chunk 的 `[start, end]`
  - 写入方法：给定 chunk，读取源文件对应区间并写到目标文件对应区间
  - 先用“两个线程”跑通即可

验收标准
- 你能输出每个线程的 start/end，确保没有重叠/遗漏

---

## Day 47（综合案例核心：MultiThreadFileDownloader）

学习要点
- 线程划分：start/end
- 并发写：目标文件预先分配长度（至少确保可写）
- 线程等待：`join`

任务卡（70min）
- 写 `MultiThreadFileDownloader`
  - 输入：源文件路径、目标文件路径、线程数（参数或固定值都行）
  - 逻辑：多线程分块拷贝（用 RandomAccessFile 写入）
  - 输出：每个线程完成提示 + 总耗时
  - 最后做校验：至少对比源/目标文件大小一致

验收标准
- 复制可以稳定成功，多次运行结果一致

---

## Day 48（增强：异常处理与进度统计）

学习要点
- 线程异常不会自动让主线程知道：需要捕获并汇报
- 进度统计：可用 `AtomicInteger` 或 `synchronized` 汇总已完成字节

任务卡（70min）
- 在 `MultiThreadFileDownloader` 中增加
  - 任一线程失败：主线程能打印原因并结束
  - 增加进度输出：已完成百分比（粗粒度即可）

验收标准
- 故意制造一个错误（例如目标路径不可写）时程序能给出清晰提示而不是静默失败

---

## Day 49（收尾：多线程下载器（HTTP版可选）+ 周复盘）

必做（你已有文件版）
- 确保 `MultiThreadFileDownloader` 版本最终稳定通过

选做（如果网络可用）
- 改造为 HTTP Range 下载器（`Range: bytes=start-end`）
- 若网络不可用，用本地文件版继续即可（在笔记里写清原因与替代方案）

验收标准
- 周复盘文档回答：你今天掌握的“线程间通信”和“并发分块写”分别解决了什么问题

本周复盘（30min）
- 写下你最容易混淆的 5 个点（wait/notify、临界区、可见性、分块偏移、校验）
- 写下本周最常复用的 3 个模板（chunk计算模板、join等待模板、二进制拷贝模板）

