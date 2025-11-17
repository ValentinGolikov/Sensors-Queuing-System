package Engine.Devices;

import Engine.Request;
import Engine.Priority;
<<<<<<< HEAD
=======
import Engine.Threads.ThreadPauser;
import Engine.Tracking.RequestTracker;
>>>>>>> 148240b (auto_mode_v0.3.1)

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;

public abstract class Device implements Runnable {
    protected final String name;
    protected final BlockingQueue<Request> processingQueue;
    protected final AtomicBoolean running;
    protected final AtomicInteger processedCount;
    protected final AtomicBoolean isBusy;
<<<<<<< HEAD
=======
    protected Request currentRequest;
    protected final Random random;
>>>>>>> 148240b (auto_mode_v0.3.1)

    // Базовое время обработки для каждого типа заявок (в миллисекундах)
    protected static final int CRITICAL_PROCESSING_TIME = 2000;
    protected static final int WARNING_PROCESSING_TIME = 1500;
    protected static final int METRICS_PROCESSING_TIME = 1000;

    // Параметр лямбда для экспоненциального распределения
    protected static final double LAMBDA = 0.001; // 0.001 соответствует среднему времени 1000 мс

    public Device(String name) {
        this.name = name;
        this.processingQueue = new LinkedBlockingQueue<>();
        this.running = new AtomicBoolean(true);
        this.processedCount = new AtomicInteger(0);
        this.isBusy = new AtomicBoolean(false);
        this.random = new Random();
    }

    @Override
    public void run() {
        System.out.println(name + " запущен");

        while (running.get() || !processingQueue.isEmpty()) {
            try {
                ThreadPauser.checkPause();
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

            // Логика обработки заявки (формирование отчета, уведомление и т.д.)
            handleRequest(request);

            processedCount.incrementAndGet();
            System.out.println(name + " завершил обработку заявки: " + request.getId());
            RequestTracker.trackProcessed(request);

        } finally {
            isBusy.set(false);
        }
    }

    /**
     * Генерирует время обработки по экспоненциальному закону
     * @param baseTime базовое время обработки для данного приоритета
     * @return время обработки с экспоненциальным распределением
     */
    protected int getExponentialProcessingTime(int baseTime) {
        // Генерируем случайную величину с экспоненциальным распределением
        double exponentialValue = -Math.log(1 - random.nextDouble()) / LAMBDA;

        // Масштабируем базовое время с учетом экспоненциального значения
        // Ограничиваем максимальное время разумным значением (10 * baseTime)
        int processingTime = (int) (baseTime * (1 + exponentialValue / 1000));
        return Math.min(processingTime, baseTime * 10);
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