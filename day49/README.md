## Day49：收尾验证（MultiThreadFileDownloader）

### 运行（使用 Day48 的 V2）

在项目根目录执行：

```bash
javac day48/*.java
java -cp day48 MultiThreadFileDownloaderV2 day45/source.bin day49/out.bin 4
```

### 验证（大小 + hash）

```bash
ls -l day45/source.bin day49/out.bin
sha256sum day45/source.bin day49/out.bin
```

