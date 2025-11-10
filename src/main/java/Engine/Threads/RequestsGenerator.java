package Engine.Threads;

import Engine.Controller;
import Engine.Request;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestsGenerator {
    private static final BlockingQueue<Request> requestQueue = new LinkedBlockingQueue<>();
    private static final AtomicInteger totalGenerated = new AtomicInteger(0);
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static Controller controller = null;

    public RequestsGenerator(Controller controller) {
        RequestsGenerator.controller = controller;
    }

    public static void start() throws InterruptedException {
        System.out.println("=== ЗАПУСК ГЕНЕРАТОРА ЗАЯВОК ===");

        Thread criticalThread = new Thread(new CriticalGenerator(running, controller), "Critical-Generator");
        Thread warningThread = new Thread(new WarningGenerator(running, controller), "Warning-Generator");
        Thread metricsThread = new Thread(new MetricsGenerator(running, controller), "Metrics-Generator");

        criticalThread.start();
        warningThread.start();
        metricsThread.start();

        Thread.sleep(10000);

        // Останавливаем генерацию
        running.set(false);
        System.out.println("\n=== ОСТАНОВКА ГЕНЕРАЦИИ ===");

        // Ждем завершения потоков
        criticalThread.join();
        warningThread.join();
        metricsThread.join();

        System.out.println("Всего сгенерировано заявок: " + totalGenerated.get());
    }
}
