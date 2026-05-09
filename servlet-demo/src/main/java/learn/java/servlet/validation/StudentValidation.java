package learn.java.servlet.validation;

import java.util.regex.Pattern;

/** Day124：学生字段校验（后端双保险）；与前端 {@code RegExp} 规则对齐。 */
public final class StudentValidation {

    private static final Pattern PHONE_CN = Pattern.compile("^1[3-9]\\d{9}$");

    private StudentValidation() {}

    /** 姓名：非空，长度 1～20（{@code (?s)} 使 {@code .} 含换行，避免误伤）。 */
    public static String validateName(String trimmedName) {
        if (trimmedName == null || trimmedName.isEmpty()) {
            return "name 不能为空";
        }
        if (!trimmedName.matches("(?s)^.{1,20}$")) {
            return "name 长度须在 1～20";
        }
        return null;
    }

    /** 分数：0～100 的整数（调用方已保证为 int）。 */
    public static String validateScore(int score) {
        if (score < 0 || score > 100) {
            return "score 须在 0～100 的整数";
        }
        return null;
    }

    /** 大陆手机号（课堂简化）：{@code 1[3-9] + 9 位数字}。 */
    public static String validatePhone(String trimmedPhone) {
        if (trimmedPhone == null || trimmedPhone.isEmpty()) {
            return "phone 不能为空";
        }
        if (!PHONE_CN.matcher(trimmedPhone).matches()) {
            return "phone 须为 11 位大陆手机号（简化：首位 1，第二位 3-9）";
        }
        return null;
    }

    public static String validateAge(int age) {
        if (age < 1 || age > 120) {
            return "age 须在 1～120";
        }
        return null;
    }
}
