public class LazySingleton {
    private static LazySingleton instance;

    private LazySingleton() {}

    // Day25 基础版：懒加载（单线程/学习用）
    public static LazySingleton getInstance() {
        if (instance == null) {
            instance = new LazySingleton();
        }
        return instance;
    }
}

