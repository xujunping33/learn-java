import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class ChunkedCopySupport {
    public static void main(String[] args) throws Exception {
        String src = "day45/source.bin";
        String dst = "day46/chunk_copy.bin";
        int threadCount = 2;

        long size = new File(src).length();
        if (size <= 0) {
            throw new IllegalStateException("源文件为空或不存在: " + src);
        }

        List<Chunk> chunks = splitToChunks(size, threadCount);
        System.out.println("src=" + src + ", size=" + size + ", threads=" + threadCount);
        for (Chunk c : chunks) {
            System.out.println(c);
        }

        // 预分配目标文件长度（RandomAccessFile 写入更稳）
        preAllocate(dst, size);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            Chunk c = chunks.get(i);
            Thread t = new Thread(() -> {
                try {
                    copyChunk(src, dst, c);
                    System.out.println(Thread.currentThread().getName() + " done " + c);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, "Chunk-" + (i + 1));
            threads.add(t);
        }

        long t1 = System.nanoTime();
        for (Thread t : threads) t.start();
        for (Thread t : threads) joinQuietly(t);
        long t2 = System.nanoTime();

        long dstSize = new File(dst).length();
        System.out.println("dst size=" + dstSize + ", size equal? " + (dstSize == size));
        System.out.println("elapsed(ms)=" + ((t2 - t1) / 1_000_000.0));
    }

    // chunk 区间： [start, end) ，end 不包含
    public static List<Chunk> splitToChunks(long fileSize, int threadCount) {
        if (threadCount <= 0) throw new IllegalArgumentException("threadCount must be >0");
        List<Chunk> out = new ArrayList<>();
        long chunkSize = (fileSize + threadCount - 1) / threadCount; // ceil
        long start = 0;
        while (start < fileSize) {
            long end = Math.min(start + chunkSize, fileSize);
            out.add(new Chunk(start, end));
            start = end;
        }
        return out;
    }

    public static void copyChunk(String src, String dst, Chunk chunk) throws IOException {
        try (RandomAccessFile in = new RandomAccessFile(src, "r");
             RandomAccessFile out = new RandomAccessFile(dst, "rw")) {
            in.seek(chunk.start);
            out.seek(chunk.start);

            long remaining = chunk.length();
            byte[] buf = new byte[8192];
            while (remaining > 0) {
                int toRead = (int) Math.min(buf.length, remaining);
                int len = in.read(buf, 0, toRead);
                if (len == -1) break;
                out.write(buf, 0, len);
                remaining -= len;
            }
        }
    }

    private static void preAllocate(String dst, long size) throws IOException {
        File f = new File(dst);
        File parent = f.getParentFile();
        if (parent != null) parent.mkdirs();
        try (RandomAccessFile raf = new RandomAccessFile(dst, "rw")) {
            raf.setLength(size);
        }
    }

    private static void joinQuietly(Thread t) {
        try {
            t.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static class Chunk {
        public final long start;
        public final long end;

        public Chunk(long start, long end) {
            this.start = start;
            this.end = end;
        }

        public long length() {
            return end - start;
        }

        @Override
        public String toString() {
            return "Chunk{start=" + start + ", end=" + end + ", len=" + length() + "}";
        }
    }
}

