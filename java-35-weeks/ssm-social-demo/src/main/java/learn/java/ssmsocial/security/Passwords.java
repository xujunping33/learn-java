package learn.java.ssmsocial.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/** Day164：demo 级密码哈希（salt + SHA-256），不追求生产强度。 */
public final class Passwords {

    private static final SecureRandom RND = new SecureRandom();

    private Passwords() {}

    public static String newSaltHex() {
        byte[] b = new byte[16];
        RND.nextBytes(b);
        return toHex(b);
    }

    public static String hashHex(String saltHex, String password) {
        if (saltHex == null || saltHex.isBlank()) {
            throw new IllegalArgumentException("salt is required");
        }
        if (password == null) {
            throw new IllegalArgumentException("password is required");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(saltHex.getBytes(StandardCharsets.UTF_8));
            md.update((byte) ':');
            md.update(password.getBytes(StandardCharsets.UTF_8));
            return toHex(md.digest());
        } catch (Exception e) {
            throw new IllegalStateException("hash failed", e);
        }
    }

    public static boolean verify(String saltHex, String expectedHashHex, String password) {
        if (expectedHashHex == null) {
            return false;
        }
        return expectedHashHex.equalsIgnoreCase(hashHex(saltHex, password));
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}

