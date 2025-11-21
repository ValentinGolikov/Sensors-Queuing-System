package Engine.Devices;

import Engine.Request;
import Engine.Priority;
import Engine.Threads.ThreadPauser;
import Engine.Tracking.RequestTracker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;

public abstract class Device implements Runnable {
    protected final String name;
    protected final BlockingQueue<Request> processingQueue;
    protected final AtomicBoolean running;
    protected final AtomicInteger processedCount;
    protected final AtomicBoolean isBusy;
    protected Request currentRequest;
    protected final Random random;

    protected static final long TIMEOUT = 10000;

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
                Request request = processingQueue.poll(TIMEOUT, TimeUnit.MILLISECONDS);
                if (request != null) processRequest(request);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println(name + " завершен. Обработано заявок: " + processedCount.get());
    }

    protected void processRequest(Request request) {
        isBusy.set(true);
        currentRequest = request;

        RequestTracker.trackInDevice(request, name);
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

    public Request getCurrentRequest() { return currentRequest; }
}