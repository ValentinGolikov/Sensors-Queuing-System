package Engine.Threads;

import Engine.Controller;
import Engine.Tracking.ManualModeController;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestsGenerator implements Runnable {
    private final AtomicInteger totalGenerated = new AtomicInteger(0);
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private final Controller controller;
    private static boolean stopFlag = false;

    private Thread criticalThread;
    private Thread warningThread;
    private Thread metricsThread;

    public RequestsGenerator(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        System.out.println("=== ЗАПУСК ГЕНЕРАТОРА ЗАЯВОК ===");

        criticalThread = new Thread(new CriticalGenerator(running, controller, totalGenerated), "Critical-Generator");
        warningThread = new Thread(new WarningGenerator(running, controller, totalGenerated), "Warning-Generator");
        metricsThread = new Thread(new MetricsGenerator(running, controller, totalGenerated), "Metrics-Generator");

        metricsThread.start();
        warningThread.start();
        criticalThread.start();

        try {
            // Ждем завершения всех потоков-генераторов
            metricsThread.join();
            warningThread.join();
            criticalThread.join();

            System.out.println("Всего сгенерировано заявок: " + totalGenerated.get());
            System.out.println("=== ГЕНЕРАТОР ЗАЯВОК ЗАВЕРШЕН ===");

        } catch (InterruptedException e) {
            System.err.println("Генератор заявок прерван: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        running.set(false);
        System.out.println("\n=== ОСТАНОВКА ГЕНЕРАЦИИ ===");
        stopFlag = true;

        // Прерываем потоки-генераторы на случай, если они спят
        if (criticalThread != null) criticalThread.interrupt();
        if (warningThread != null) warningThread.interrupt();
        if (metricsThread != null) metricsThread.interrupt();
    }

    public static boolean isStopped() {
        return stopFlag;
    }
}