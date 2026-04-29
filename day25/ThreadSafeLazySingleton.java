public class ThreadSafeLazySingleton {
    // volatile：保证可见性 + 禁止指令重排（DCL 必需）
    private static volatile ThreadSafeLazySingleton instance;

    private ThreadSafeLazySingleton() {}

    public static ThreadSafeLazySingleton getInstance() {
        ThreadSafeLazySingleton local = instance;
        if (local == null) {
            synchronized (ThreadSafeLazySingleton.class) {
                local = instance;
                if (local == null) {
                    local = new ThreadSafeLazySingleton();
                    instance = local;
                }
            }
        }
        return local;
    }
}

