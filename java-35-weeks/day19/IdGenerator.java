public class IdGenerator {
    private static int nextId = 1;

    public static int next() {
        return nextId++;
    }
}

