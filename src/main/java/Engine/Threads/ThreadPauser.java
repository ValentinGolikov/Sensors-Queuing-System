package Engine.Threads;

import Engine.PauseTimeManager;

public class ThreadPauser {
    private final static Object lock = new Object();
    private static boolean paused = false;

    public static void pauseAllThreads() {
        synchronized(lock) {
            paused = true;
            PauseTimeManager.setPauseStartTime(); // Записываем время начала паузы
        }
    }

    public static void resumeAllThreads() {
        synchronized(lock) {
            paused = false;
            PauseTimeManager.setPauseEndTime(); // Записываем время окончания паузы
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
