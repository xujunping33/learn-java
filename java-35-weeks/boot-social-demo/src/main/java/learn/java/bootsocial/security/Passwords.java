package learn.java.bootsocial.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public final class Passwords {

    private static final SecureRandom rnd = new SecureRandom();

    private Passwords() {}

    public static String newSaltHex(int bytes) {
        if (bytes <= 0) {
            throw new IllegalArgumentException("bytes must be positive");
        }
        byte[] b = new byte[bytes];
        rnd.nextBytes(b);
        return hex(b);
    }

    public static String sha256Hex(String saltHex, String password) {
        if (saltHex == null || saltHex.isBlank()) {
            throw new IllegalArgumentException("salt is required");
        }
        if (password == null) {
            password = "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((saltHex + ":" + password).getBytes(StandardCharsets.UTF_8));
            return hex(md.digest());
        } catch (Exception e) {
            throw new IllegalStateException("sha-256 not available", e);
        }
    }

    public static boolean verify(String saltHex, String password, String expectedHashHex) {
        if (expectedHashHex == null) {
            return false;
        }
        return expectedHashHex.equalsIgnoreCase(sha256Hex(saltHex, password));
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte x : bytes) {
            sb.append(Character.forDigit((x >>> 4) & 0xF, 16));
            sb.append(Character.forDigit(x & 0xF, 16));
        }
        return sb.toString();
    }
}

