package Engine.Threads;

public class ThreadPauser {
    private final static Object lock = new Object();
    private static boolean paused = false;

    public static void pauseAllThreads() {
        synchronized(lock) {
            paused = true;
        }
    }

    public static void resumeAllThreads() {
        synchronized(lock) {
            paused = false;
            lock.notifyAll();
        }
    }

    public static void checkPause() throws InterruptedException {
        synchronized(lock) {
            while (paused) {
                lock.wait();
            }
        }
    }
}
