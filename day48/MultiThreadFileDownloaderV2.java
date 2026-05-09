import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class MultiThreadFileDownloaderV2 {
    public static void main(String[] args) {
        String src = args.length >= 1 ? args[0] : "day45/source.bin";
        String dst = args.length >= 2 ? args[1] : "day48/download_v2.bin";
        int threads = args.length >= 3 ? parseIntOrDefault(args[2], 4) : 4;
        if (threads < 1) threads = 1;

        File srcFile = new File(src);
        if (!srcFile.exists() || !srcFile.isFile()) {
            System.out.println("源文件不存在: " + src);
            return;
        }

        long size = srcFile.length();
        if (size == 0) {
            System.out.println("源文件为空: " + src);
            return;
        }

        List<Chunk> chunks = splitToChunks(size, threads);
        System.out.println("=== MultiThreadFileDownloaderV2 ===");
        System.out.println("src=" + src);
        System.out.println("dst=" + dst);
        System.out.println("size=" + size);
        System.out.println("threads=" + chunks.size());

        try {
            preAllocate(dst, size);
        } catch (IOException e) {
            System.out.println("目标文件预分配失败: " + e.getMessage());
            return;
        }

        AtomicLong doneBytes = new AtomicLong(0);
        AtomicReference<Throwable> firstError = new AtomicReference<>(null);

        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            Chunk c = chunks.get(i);
            Thread t = new Thread(() -> {
                try {
                    copyChunk(src, dst, c, doneBytes, firstError);
                    System.out.println(Thread.currentThread().getName() + " 完成 " + c);
                } catch (Throwable e) {
                    // 线程内异常需要“汇报”给主线程
                    firstError.compareAndSet(null, e);
                    System.out.println(Thread.currentThread().getName() + " 失败: " + e.getMessage());
                }
            }, "Worker-" + (i + 1));
            workers.add(t);
        }

        Thread progress = new Thread(() -> {
            while (true) {
                Throwable err = firstError.get();
                if (err != null) return;
                long done = doneBytes.get();
                double pct = done * 100.0 / size;
                System.out.printf("进度: %.2f%% (%d/%d)%n", pct, done, size);
                if (done >= size) return;
                sleepQuietly(200);
            }
        }, "Progress");

        long t1 = System.nanoTime();
        progress.start();
        for (Thread t : workers) t.start();
        for (Thread t : workers) joinQuietly(t);
        joinQuietly(progress);
        long t2 = System.nanoTime();

        Throwable err = firstError.get();
        if (err != null) {
            System.out.println();
            System.out.println("=== 失败 ===");
            System.out.println("原因: " + err.getClass().getSimpleName() + " - " + err.getMessage());
            return;
        }

        long dstSize = new File(dst).length();
        System.out.println();
        System.out.println("=== 校验 ===");
        System.out.println("dstSize=" + dstSize + ", sizeEqual? " + (dstSize == size));
        System.out.println("elapsed(ms)=" + ((t2 - t1) / 1_000_000.0));
    }

    private static void copyChunk(
            String src,
            String dst,
            Chunk c,
            AtomicLong doneBytes,
            AtomicReference<Throwable> firstError
    ) throws IOException {
        try (RandomAccessFile in = new RandomAccessFile(src, "r");
             RandomAccessFile out = new RandomAccessFile(dst, "rw")) {
            in.seek(c.start);
            out.seek(c.start);

            long remaining = c.length();
            byte[] buf = new byte[8192];
            while (remaining > 0) {
                if (firstError.get() != null) return; // 有线程失败，尽快停止
                int toRead = (int) Math.min(buf.length, remaining);
                int len = in.read(buf, 0, toRead);
                if (len == -1) break;
                out.write(buf, 0, len);
                remaining -= len;
                doneBytes.addAndGet(len);
            }
        }
    }

    private static int parseIntOrDefault(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static List<Chunk> splitToChunks(long fileSize, int threadCount) {
        long chunkSize = (fileSize + threadCount - 1) / threadCount;
        List<Chunk> out = new ArrayList<>();
        for (long start = 0; start < fileSize; start += chunkSize) {
            long end = Math.min(start + chunkSize, fileSize);
            out.add(new Chunk(start, end));
        }
        return out;
    }

    private static void preAllocate(String dst, long size) throws IOException {
        File f = new File(dst);
        File parent = f.getParentFile();
        if (parent != null) parent.mkdirs();
        try (RandomAccessFile raf = new RandomAccessFile(dst, "rw")) {
            raf.setLength(size);
        }
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void joinQuietly(Thread t) {
        try {
            t.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static class Chunk {
        final long start;
        final long end;

        Chunk(long start, long end) {
            this.start = start;
            this.end = end;
        }

        long length() {
            return end - start;
        }

        @Override
        public String toString() {
            return "Chunk{start=" + start + ", end=" + end + ", len=" + length() + "}";
        }
    }
}

