public class TicketDemo {
    public static void main(String[] args) {
        System.out.println("=== 不加锁版本（可能出现重复卖/负数票）===");
        runNoLock();

        System.out.println();
        System.out.println("=== 加锁版本（使用 synchronized 修复）===");
        runWithLock();
    }

    private static void runNoLock() {
        int initial = 50;
        Ticket ticket = new Ticket(initial);
        int threads = 8;
        Thread[] ts = new Thread[threads];

        java.util.concurrent.atomic.AtomicInteger sold = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> {
                while (true) {
                    // 注意：这里是典型的“检查-再更新”竞态条件
                    if (ticket.tickets > 0) {
                        int current = ticket.tickets;
                        // 放大竞态：让别的线程更可能插队
                        sleepQuietly(1);
                        ticket.tickets = current - 1;
                        sold.incrementAndGet();
                        System.out.println(Thread.currentThread().getName()
                                + " 卖出，剩余=" + ticket.tickets);
                    } else {
                        return;
                    }
                }
            }, "TNL-" + (i + 1));
        }

        for (Thread t : ts) t.start();
        for (Thread t : ts) joinQuietly(t);

        int finalRemaining = ticket.tickets;
        int soldCount = sold.get();
        System.out.println("最终剩余票=" + finalRemaining);
        System.out.println("卖出次数 sold=" + soldCount + "（不加锁时可能 > 初始票数 " + initial + "）");
        System.out.println("若按票数守恒，应满足 sold == " + initial + " - " + finalRemaining);
    }

    private static void runWithLock() {
        Object lock = new Object();
        int initial = 50;
        LockedTicket ticket = new LockedTicket(initial);
        int threads = 8;
        Thread[] ts = new Thread[threads];

        java.util.concurrent.atomic.AtomicInteger sold = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> {
                while (true) {
                    synchronized (lock) {
                        if (ticket.tickets <= 0) {
                            return;
                        }
                        sleepQuietly(1); // 在锁内也放一点延迟，便于观察
                        ticket.tickets--;
                        sold.incrementAndGet();
                        System.out.println(Thread.currentThread().getName()
                                + " 卖出，剩余=" + ticket.tickets);
                    }
                }
            }, "TLS-" + (i + 1));
        }

        for (Thread t : ts) t.start();
        for (Thread t : ts) joinQuietly(t);

        int finalRemaining = ticket.tickets;
        int soldCount = sold.get();
        System.out.println("最终剩余票=" + finalRemaining + "（应为 0）");
        System.out.println("卖出次数 sold=" + soldCount + "（应等于初始票数 " + initial + "）");
        System.out.println("若按票数守恒，应满足 sold == " + initial + " - " + finalRemaining);
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void joinQuietly(Thread t) {
        try {
            t.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static class Ticket {
        // 故意不加 synchronized：让竞态出现
        int tickets;

        Ticket(int tickets) {
            this.tickets = tickets;
        }
    }

    private static class LockedTicket {
        int tickets;

        LockedTicket(int tickets) {
            this.tickets = tickets;
        }
    }
}

