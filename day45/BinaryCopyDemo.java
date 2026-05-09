import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BinaryCopyDemo {
    public static void main(String[] args) {
        String src = "day45/source.bin";
        String dst1 = "day45/copy_nobuffer.bin";
        String dst2 = "day45/copy_buffer.bin";

        ensureSourceFile(src, 2_000_00); // 约 200KB（够演示耗时差异）

        System.out.println("src=" + src);
        System.out.println("dst1=" + dst1);
        System.out.println("dst2=" + dst2);

        try {
            long t1 = System.nanoTime();
            copyNoBuffer(src, dst1);
            long t2 = System.nanoTime();
            copyWithBuffer(src, dst2, 8192);
            long t3 = System.nanoTime();

            long s1 = new File(src).length();
            long s2 = new File(dst1).length();
            long s3 = new File(dst2).length();

            System.out.println();
            System.out.println("=== 验收 ===");
            System.out.println("源文件大小=" + s1 + ", 无 buffer 复制大小=" + s2 + ", 有 buffer 复制大小=" + s3);
            System.out.println("无 buffer 文件大小一致? " + (s1 == s2));
            System.out.println("有 buffer 文件大小一致? " + (s1 == s3));

            System.out.println();
            System.out.println("=== 耗时（粗略）=== ");
            System.out.println("无 buffer 耗时(ms) = " + ((t2 - t1) / 1_000_000.0));
            System.out.println("有 buffer 耗时(ms)  = " + ((t3 - t2) / 1_000_000.0));
        } catch (IOException e) {
            System.out.println("复制失败：" + e.getMessage());
        }
    }

    // 不加 buffer：每次 read/write 一个字节
    private static void copyNoBuffer(String src, String dst) throws IOException {
        try (FileInputStream in = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dst)) {
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
        }
    }

    // 加 buffer：批量读写
    private static void copyWithBuffer(String src, String dst, int bufSize) throws IOException {
        try (FileInputStream in = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[bufSize];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        }
    }

    private static void ensureSourceFile(String path, int bytes) {
        File f = new File(path);
        if (f.exists() && f.length() >= bytes) return;
        try (FileOutputStream out = new FileOutputStream(path)) {
            // 写入一段可预测内容
            byte b = 0;
            for (int i = 0; i < bytes; i++) {
                out.write(b);
                b++;
            }
        } catch (IOException e) {
            throw new RuntimeException("创建源文件失败：" + e.getMessage(), e);
        }
    }
}

