import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiniLogProcessor {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-zA-Z0-9]+");

    public static void main(String[] args) {
        String path = args.length >= 1 ? args[0] : "day42/log_sample.txt";
        int threadCount = args.length >= 2 ? Integer.parseInt(args[1]) : 4;
        if (threadCount < 2) threadCount = 2; // 题目要求至少 2 个线程

        String text = readAllText(path);
        List<String> tokens = tokenize(text);

        if (tokens.isEmpty()) {
            System.out.println("日志里没有可统计的 token。");
            return;
        }

        int total = tokens.size();
        int chunkSize = (total + threadCount - 1) / threadCount; // ceil

        List<Worker> workers = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, total);
            if (start >= end) break;

            Worker w = new Worker(tokens, start, end);
            Thread t = new Thread(w, "W" + (i + 1));
            workers.add(w);
            threads.add(t);
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) joinQuietly(t);

        // 合并局部统计结果
        Map<String, Integer> global = new HashMap<>();
        for (Worker w : workers) {
            for (Map.Entry<String, Integer> e : w.local.entrySet()) {
                global.merge(e.getKey(), e.getValue(), Integer::sum);
            }
        }

        // 输出统计结果（TopN）
        List<Map.Entry<String, Integer>> list = new ArrayList<>(global.entrySet());
        list.sort(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed()
                .thenComparing(Map.Entry::getKey));

        System.out.println("=== MiniLogProcessor ===");
        System.out.println("input=" + path);
        System.out.println("tokenCount=" + tokens.size());
        System.out.println("uniqueKeys=" + global.size());
        System.out.println();

        int topN = Math.min(10, list.size());
        System.out.println("Top" + topN + " 高频 token：");
        for (int i = 0; i < topN; i++) {
            Map.Entry<String, Integer> e = list.get(i);
            System.out.println((i + 1) + ". " + e.getKey() + " -> " + e.getValue());
        }
    }

    private static class Worker implements Runnable {
        private final List<String> tokens;
        private final int start;
        private final int end;
        private final Map<String, Integer> local = new HashMap<>();

        Worker(List<String> tokens, int start, int end) {
            this.tokens = tokens;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            for (int i = start; i < end; i++) {
                String t = tokens.get(i);
                local.merge(t, 1, Integer::sum);
            }
        }
    }

    private static String readAllText(String path) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + path + ", " + e.getMessage(), e);
        }
        return sb.toString();
    }

    private static List<String> tokenize(String text) {
        String lower = text.toLowerCase();
        List<String> out = new ArrayList<>();
        Matcher m = TOKEN_PATTERN.matcher(lower);
        while (m.find()) {
            out.add(m.group());
        }
        return out;
    }

    private static void joinQuietly(Thread t) {
        try {
            t.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

