package learn.java.maven;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringUtilsTest {

    @Test
    void reverse_normal() {
        assertEquals("cba", StringUtils.reverse("abc"));
    }

    @Test
    void reverse_empty() {
        assertEquals("", StringUtils.reverse(""));
    }

    @Test
    void reverse_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.reverse(null));
    }

    @Test
    void clampScore_outOfRange() {
        assertEquals(0, StringUtils.clampScore(-10));
        assertEquals(100, StringUtils.clampScore(999));
    }
}

