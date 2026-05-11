package learn.java.oa.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 与 {@code sql/oa_seed.sql} 一致：<strong>MD5(UTF-8 明文密码 + salt)</strong>，32 位小写十六进制。
 * 生产环境更推荐 BCrypt；本周按课纲。
 */
public final class Passwords {

    private Passwords() {}

    public static String md5HexPasswordPlusSalt(String plainPassword, String salt) throws Exception {
        String concat = plainPassword + salt;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(concat.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
