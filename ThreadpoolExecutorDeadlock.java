import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadpoolExecutorDeadlock {
    public static final Object account1 = new Object();
    public static final Object account2 = new Object();
    public static final Object account3 = new Object();

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // transaction from account1 to account2
        Runnable task1 = new Task1();

        // transaction from account2 to account3
        Runnable task2 = new Task2();

        // transaction from account3 to account1
        Runnable task3 = new Task3();

        executor.submit(task1);
        executor.submit(task2);
        executor.submit(task3);

        executor.shutdown();


        try {
            // waiting to increase changes of deadlock detection
            Thread.sleep(2000);

            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            long[] deadlockedThreadIds = threadMXBean.findDeadlockedThreads();

            if (deadlockedThreadIds != null && deadlockedThreadIds.length > 0) {
                System.out.println("Deadlocked Threads:");
                ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(deadlockedThreadIds);
                for (ThreadInfo threadInfo : threadInfos) {
                    System.out.println(threadInfo.getThreadId() + ": " + threadInfo.getThreadName());
                }
            } else {
                System.out.println("No deadlocked threads found.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

class Task1 implements Runnable {
    @Override
    public void run() {
        synchronized (ThreadpoolExecutorDeadlock.account1) {
            System.out.println("Task 1 acquired lock on (Account 1)");

            try {
                Thread.sleep(100);  // Introducing a delay to exaggerate the deadlock situation
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (ThreadpoolExecutorDeadlock.account2) {
                System.out.println("Task 1 acquired lock on (Account 2)");
                // Perform transfer logic between account 1 and account 2
            }
        }
    }
}

class Task2 implements Runnable {
    @Override
    public void run() {
        synchronized (ThreadpoolExecutorDeadlock.account2) {
            System.out.println("Task 2 acquired lock on (Account 2)");

            synchronized (ThreadpoolExecutorDeadlock.account3) {
                System.out.println("Task 2 acquired lock on (Account 3)");
                // Perform transfer logic between account 2 and account 3
            }
        }
    }
}

class Task3 implements Runnable {
    @Override
    public void run() {
        synchronized (ThreadpoolExecutorDeadlock.account3) {
            System.out.println("Task 3 acquired lock on (Account 3)");

            synchronized (ThreadpoolExecutorDeadlock.account1) {
                System.out.println("Task 3 acquired lock on (Account 1)");
                // Perform transfer logic between account 3 and account 1
            }
        }
    }
}


// code to create your own custom thread factory which creates manages and retursns new therads

//public class CustomThreadFactory implements ThreadFactory {
//    private int threadCount;
//    private String threadNamePrefix;
//
//    public CustomThreadFactory(String threadNamePrefix) {
//        this.threadCount = 1;
//        this.threadNamePrefix = threadNamePrefix;
//    }
//
//    public Thread newThread(Runnable runnable) {
//        Thread thread = new Thread(runnable);
//        thread.setName(threadNamePrefix + "-" + threadCount++);
//        thread.setPriority(Thread.NORM_PRIORITY);
//        thread.setDaemon(false);
//        return thread;
//    }
//}
//
//    ThreadFactory customThreadFactory = new CustomThreadFactory("MyThread");
//    ExecutorService executor = Executors.newFixedThreadPool(5, customThreadFactory);
