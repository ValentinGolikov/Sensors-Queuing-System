package Engine.Devices;

import Engine.Request;
import Engine.Priority;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Device implements Runnable {
    protected final String name;
    protected final BlockingQueue<Request> processingQueue;
    protected final AtomicBoolean running;
    protected final AtomicInteger processedCount;
    protected final AtomicBoolean isBusy;

    // Время обработки для каждого типа заявок (в миллисекундах)
    protected static final int CRITICAL_PROCESSING_TIME = 2000;
    protected static final int WARNING_PROCESSING_TIME = 1500;
    protected static final int METRICS_PROCESSING_TIME = 1000;

    public Device(String name) {
        this.name = name;
        this.processingQueue = new LinkedBlockingQueue<>();
        this.running = new AtomicBoolean(true);
        this.processedCount = new AtomicInteger(0);
        this.isBusy = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        System.out.println(name + " запущен");

        while (running.get() || !processingQueue.isEmpty()) {
            try {
                Request request = processingQueue.take();
                processRequest(request);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println(name + " завершен. Обработано заявок: " + processedCount.get());
    }

    protected void processRequest(Request request) {
        isBusy.set(true);

        try {
            System.out.println(name + " обрабатывает заявку: " + request.getId() + " с приоритетом: " + request.getPriority());

            // Имитация времени обработки в зависимости от приоритета
            int processingTime = getProcessingTime(request.getPriority());
            Thread.sleep(processingTime);

            // Логика обработки заявки (формирование отчета, уведомление и т.д.)
            handleRequest(request);

            processedCount.incrementAndGet();
            System.out.println(name + " завершил обработку заявки: " + request.getId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            isBusy.set(false);
        }
    }

    protected int getProcessingTime(Priority priority) {
        switch (priority) {
            case CRITICAL: return CRITICAL_PROCESSING_TIME;
            case WARNING: return WARNING_PROCESSING_TIME;
            case METRICS: return METRICS_PROCESSING_TIME;
            default: return 1000;
        }
    }

    // Абстрактный метод для конкретной реализации обработки
    protected abstract void handleRequest(Request request);

    // Добавление заявки в очередь обработки
    public boolean submitRequest(Request request) {
        return processingQueue.offer(request);
    }

    // Проверка, свободен ли прибор
    public boolean isAvailable() {
        return !isBusy.get() && processingQueue.isEmpty();
    }

    // Проверка, может ли прибор обработать данный тип заявки
    public abstract boolean canHandle(Priority priority);

    // Остановка устройства
    public void stop() {
        running.set(false);
    }

    public String getName() {
        return name;
    }

    public int getProcessedCount() {
        return processedCount.get();
    }
}