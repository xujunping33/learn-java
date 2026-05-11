public class SingletonUseDemo {
    public static void main(String[] args) {
        System.out.println("== 饿汉单例 ==");
        EagerSingleton e1 = EagerSingleton.getInstance();
        EagerSingleton e2 = EagerSingleton.getInstance();
        System.out.println("e1 == e2 ? " + (e1 == e2));
        System.out.println("e1.hashCode = " + e1.hashCode());
        System.out.println("e2.hashCode = " + e2.hashCode());

        System.out.println();
        System.out.println("== 懒汉单例（基础版） ==");
        LazySingleton l1 = LazySingleton.getInstance();
        LazySingleton l2 = LazySingleton.getInstance();
        System.out.println("l1 == l2 ? " + (l1 == l2));
        System.out.println("l1.hashCode = " + l1.hashCode());
        System.out.println("l2.hashCode = " + l2.hashCode());

        System.out.println();
        System.out.println("== 懒汉单例（线程安全：DCL + volatile）==");
        ThreadSafeLazySingleton ts1 = ThreadSafeLazySingleton.getInstance();
        ThreadSafeLazySingleton ts2 = ThreadSafeLazySingleton.getInstance();
        System.out.println("ts1 == ts2 ? " + (ts1 == ts2));
        System.out.println("ts1.hashCode = " + ts1.hashCode());
        System.out.println("ts2.hashCode = " + ts2.hashCode());

        System.out.println();
        System.out.println("== 多线程压力测试（应只出现同一个 hashCode）==");
        multithreadSmoke();
    }

    private static void multithreadSmoke() {
        int threads = 20;
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> {
                ThreadSafeLazySingleton s = ThreadSafeLazySingleton.getInstance();
                System.out.println(Thread.currentThread().getName() + " -> " + s.hashCode());
            }, "T" + (i + 1));
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

