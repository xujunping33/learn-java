public class StringApiPractice {
    public static void main(String[] args) {
        String s = "  A man a plan a canal Panama  ";
        String email = "test@example.com";
        String phone = "13800138000";
        String path = "/a/b/c.txt";

        System.out.println("1) length = " + s.length());
        System.out.println("2) charAt(2) = " + s.charAt(2));
        System.out.println("3) substring(2,7) = " + s.substring(2, 7));
        System.out.println("4) indexOf(\"plan\") = " + s.indexOf("plan"));
        System.out.println("5) contains(\"canal\") = " + s.contains("canal"));
        System.out.println("6) startsWith(\"  A\") = " + s.startsWith("  A"));
        System.out.println("7) endsWith(\"  \") = " + s.endsWith("  "));
        System.out.println("8) replace(\"Panama\",\"JAVA\") = " + s.replace("Panama", "JAVA"));

        String trimmed = s.trim();
        System.out.println("9) trim = [" + trimmed + "]");

        String compressed = compressSpaces("  hello   java   world  ");
        System.out.println("10) compressSpaces = [" + compressed + "]");

        System.out.println("11) isPalindrome(\"level\") = " + isPalindrome("level"));
        System.out.println("12) countChar('a') in trimmed = " + countChar(trimmed.toLowerCase(), 'a'));
        System.out.println("13) equalsIgnoreCase(\"abc\",\"ABC\") = " + "abc".equalsIgnoreCase("ABC"));
        System.out.println("14) simple email valid? " + isSimpleEmail(email));
        System.out.println("15) simple phone valid? " + isSimplePhone(phone));
        System.out.println("16) fileName from path = " + fileName(path));
    }

    // 回文：忽略大小写与非字母数字
    public static boolean isPalindrome(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = Character.toLowerCase(s.charAt(i));
            if (Character.isLetterOrDigit(c)) sb.append(c);
        }
        String cleaned = sb.toString();
        int l = 0, r = cleaned.length() - 1;
        while (l < r) {
            if (cleaned.charAt(l) != cleaned.charAt(r)) return false;
            l++;
            r--;
        }
        return true;
    }

    public static int countChar(String s, char target) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == target) count++;
        }
        return count;
    }

    // 去两端空格并把中间连续空白压成单空格（简版）
    public static String compressSpaces(String s) {
        String[] parts = s.trim().split("\\s+");
        return String.join(" ", parts);
    }

    // 最简邮箱规则：包含且只包含一个 @，@ 后还有 .
    public static boolean isSimpleEmail(String s) {
        int at = s.indexOf('@');
        if (at <= 0) return false;
        if (at != s.lastIndexOf('@')) return false;
        int dot = s.indexOf('.', at);
        return dot > at + 1 && dot < s.length() - 1;
    }

    // 最简手机号规则：11位且全数字，首位 1
    public static boolean isSimplePhone(String s) {
        if (s == null || s.length() != 11 || s.charAt(0) != '1') return false;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }

    public static String fileName(String path) {
        int idx = path.lastIndexOf('/');
        return idx >= 0 ? path.substring(idx + 1) : path;
    }
}

