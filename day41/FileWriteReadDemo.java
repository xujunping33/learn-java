import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class FileWriteReadDemo {
    public static void main(String[] args) {
        String charFile = "day41/char_input.txt";
        String charOutFile = "day41/char_readback.txt";
        String byteCopyFile = "day41/byte_copy.txt";

        System.out.println("== 1) 字符流写入文本 -> 读取回显示 ==");
        writeTextWithCharStream(charFile);
        String readBack = readTextWithCharStream(charFile);
        System.out.println("读取内容（来自 char_input.txt）如下：");
        System.out.println(readBack);

        System.out.println();
        System.out.println("== 2) 字节流拷贝 txt 文件 -> 校验 ==");
        try {
            copyBytes(charFile, byteCopyFile);
            long srcSize = fileSize(charFile);
            long dstSize = fileSize(byteCopyFile);
            System.out.println("源文件大小=" + srcSize + ", 复制后大小=" + dstSize);

            // 额外校验：比较字符读取结果是否一致（更贴近“内容一致”）
            String copiedText = readTextWithCharStream(byteCopyFile);
            System.out.println("复制内容与原内容一致? " + readBack.equals(copiedText));
        } catch (IOException e) {
            System.out.println("文件操作失败：" + e.getMessage());
        }

        System.out.println();
        System.out.println("== 3) 通过字符流把读回内容再写到另一个文件（演示写入/覆盖）==");
        try {
            try (Writer w = new BufferedWriter(new FileWriter(charOutFile))) {
                w.write(readBack);
            }
            System.out.println("已写入：" + charOutFile);
        } catch (IOException e) {
            System.out.println("写入失败：" + e.getMessage());
        }
    }

    private static void writeTextWithCharStream(String path) {
        try (Writer w = new BufferedWriter(new FileWriter(path))) {
            w.write("Hello Java I/O!");
            w.write(System.lineSeparator());
            w.write("Line 2: 这是使用字符流写入的内容。");
            w.write(System.lineSeparator());
            w.write("Done.");
        } catch (IOException e) {
            System.out.println("字符流写入失败：" + e.getMessage());
        }
    }

    private static String readTextWithCharStream(String path) {
        StringBuilder sb = new StringBuilder();
        try (Reader r = new BufferedReader(new FileReader(path))) {
            char[] buf = new char[1024];
            int len;
            while ((len = r.read(buf)) != -1) {
                sb.append(buf, 0, len);
            }
        } catch (IOException e) {
            System.out.println("字符流读取失败：" + e.getMessage());
        }
        return sb.toString();
    }

    private static void copyBytes(String srcPath, String dstPath) throws IOException {
        try (InputStream in = new FileInputStream(srcPath);
             OutputStream out = new FileOutputStream(dstPath)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        }
    }

    private static long fileSize(String path) {
        java.io.File f = new java.io.File(path);
        return f.length();
    }
}

