package learn.java.springcoredemo;

import java.time.Clock;
import java.util.UUID;

/** Tiny domain helper: shows a bean depending on another bean ({@link Clock}). */
public final class UuidGenerator {

    private final Clock clock;

    public UuidGenerator(Clock clock) {
        this.clock = clock;
    }

    public String next() {
        return UUID.randomUUID() + "@" + clock.instant().toEpochMilli();
    }
}
