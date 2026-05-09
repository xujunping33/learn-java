public class ThreadCreateDemo {
    public static void main(String[] args) {
        System.out.println("== 1) 继承 Thread ==");
        Thread t1 = new MyThread();
        t1.start();

        System.out.println("== 2) 实现 Runnable ==");
        Thread t2 = new Thread(new MyRunnable(), "Runnable-Thread");
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("主线程：两个子线程都结束了。");
    }

    static class MyThread extends Thread {
        @Override
        public void run() {
            for (int i = 1; i <= 3; i++) {
                System.out.println(getName() + " running i=" + i);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    static class MyRunnable implements Runnable {
        @Override
        public void run() {
            for (int i = 1; i <= 3; i++) {
                System.out.println(Thread.currentThread().getName() + " running i=" + i);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}

