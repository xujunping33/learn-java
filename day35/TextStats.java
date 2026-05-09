import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TextStats {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入一段文本（单行）：");
        String text = scanner.nextLine();

        int totalChars = text.length();
        int letters = 0;
        int digits = 0;
        int whitespaces = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetter(c)) letters++;
            else if (Character.isDigit(c)) digits++;
            else if (Character.isWhitespace(c)) whitespaces++;
        }

        String trimmed = text.trim();
        String[] words = trimmed.isEmpty() ? new String[0] : trimmed.split("\\s+");
        int wordCount = words.length;

        String longestWord = "";
        Map<String, Integer> freq = new HashMap<>();
        for (String w : words) {
            String norm = w.toLowerCase();
            if (norm.length() > longestWord.length()) {
                longestWord = norm;
            }
            freq.put(norm, freq.getOrDefault(norm, 0) + 1);
        }

        String topWord = "";
        int topCount = 0;
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            if (e.getValue() > topCount) {
                topWord = e.getKey();
                topCount = e.getValue();
            }
        }

        System.out.println();
        System.out.println("=== TextStats ===");
        System.out.println("字符总数 = " + totalChars);
        System.out.println("字母数 = " + letters);
        System.out.println("数字数 = " + digits);
        System.out.println("空白数 = " + whitespaces);
        System.out.println("单词数 = " + wordCount);
        System.out.println("最长单词 = " + (longestWord.isEmpty() ? "<none>" : longestWord));
        System.out.println("高频词Top1 = " + (topWord.isEmpty() ? "<none>" : (topWord + " (" + topCount + ")")));

        scanner.close();
    }
}

