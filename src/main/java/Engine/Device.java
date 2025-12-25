package Engine;

import Engine.Threads.ThreadPauser;
import Engine.Tracking.RequestTracker;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;

public class Device implements Runnable {
    protected final String name;
    protected final BlockingQueue<Request> processingQueue;
    protected final AtomicBoolean running;
    protected final AtomicInteger processedCount;
    protected final AtomicBoolean isBusy;
    protected Request currentRequest;
    protected final Random random;
    private long timeOnDeviceCritical = Duration.ZERO.toMillis();
    private long timeOnDeviceWarning = Duration.ZERO.toMillis();
    private long timeOnDeviceMetrics = Duration.ZERO.toMillis();

    private final ArrayList<Long> reqTimeOnDeviceCritical = new ArrayList<>();
    private final ArrayList<Long> reqTimeOnDeviceWarning = new ArrayList<>();
    private final ArrayList<Long> reqTimeOnDeviceMetrics = new ArrayList<>();
    private DateTime timeStart;
    private final int devicePause;

    private long busyTime;

    protected static final long TIMEOUT = 10000;
    public Device(String name, int devicePause) {
        this.name = name;
        this.processingQueue = new LinkedBlockingQueue<>(1);
        this.running = new AtomicBoolean(true);
        this.processedCount = new AtomicInteger(0);
        this.isBusy = new AtomicBoolean(false);
        this.random = new Random();
        this.devicePause = devicePause;
    }

    @Override
    public void run() {
        //System.out.println(name + " запущен");

        while (running.get() || !processingQueue.isEmpty()) {
            try {
                //ThreadPauser.checkPause();
                Request request = processingQueue.poll(TIMEOUT, TimeUnit.MILLISECONDS);
                timeStart = new DateTime();
                if (request != null) processRequest(request);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        //System.out.println(name + " завершен. Обработано заявок: " + processedCount.get());
    }

    protected void processRequest(Request request) {
        isBusy.set(true);
        DateTime startPoint = new DateTime();
        currentRequest = request;

        RequestTracker.trackInDevice(request, name);
        try {
            //System.out.println(name + " обрабатывает заявку: " + request.getId());

            switch (request.getPriority()) {
                case CRITICAL:
                    handleCriticalRequest(request);
                    reqTimeOnDeviceCritical.add(timeStart.getDifferenceFromNow());
                    timeOnDeviceCritical += timeStart.getDifferenceFromNow();
                    break;
                case WARNING:
                    handleWarningRequest(request);
                    reqTimeOnDeviceWarning.add(timeStart.getDifferenceFromNow());
                    timeOnDeviceWarning += timeStart.getDifferenceFromNow();
                    break;
                case METRICS:
                    handleMetricsRequest(request);
                    reqTimeOnDeviceMetrics.add(timeStart.getDifferenceFromNow());
                    timeOnDeviceMetrics += timeStart.getDifferenceFromNow();
                    break;
            }

            processedCount.incrementAndGet();
            //System.out.println(name + " завершил обработку заявки: " + request.getId());
            request.setStatus(RequestStatus.PROCESSED);
            RequestTracker.trackProcessed(request);
        } catch (InterruptedException e) {
            System.err.println(e);
        } finally {
            isBusy.set(false);
            busyTime += startPoint.getDifferenceFromNow() + 10;
        }
    }

    // Абстрактный метод для конкретной реализации обработки
    public void handleCriticalRequest(Request request) throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        ThreadPauser.checkPause();
        // Формирование критического отчета или отчета-предупреждения
        if (request.getPriority() == Priority.CRITICAL) {
            //System.out.println(threadName + ": Формирование КРИТИЧЕСКОГО отчета для заявки " + request.getId());
            NotificationSystem.sendNotification(request, getName());
        } else {
            //System.out.println(threadName + ": Формирование отчета-ПРЕДУПРЕЖДЕНИЯ для заявки " + request.getId());
            NotificationSystem.sendNotification(request, getName());
        }
        // Сохранение в базу данных
        saveToDatabase(request);

        //System.out.println(threadName + ": sleeping for " + (long) Math.exp((double)getProcessedCount()/1000));
        Thread.sleep((long) Math.exp((double) getProcessedCount()/devicePause));
        //Thread.sleep(devicePause + 100);
    }

    public void handleWarningRequest(Request request) throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        ThreadPauser.checkPause();

        // Формирование отчета-предупреждения
        //System.out.println(threadName + ": Формирование отчета-ПРЕДУПРЕЖДЕНИЯ для заявки " + request.getId());

        // Отправка уведомления инженеру
        NotificationSystem.sendNotification(request, getName());

        // Сохранение в базу данных
        saveToDatabase(request);
        //System.out.println(threadName + ": sleeping for " + (long) Math.exp((double)getProcessedCount()/1000));
        Thread.sleep((long) Math.exp((double) getProcessedCount()/devicePause));
        //Thread.sleep(devicePause + 100);
    }

    public void handleMetricsRequest(Request request) throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        ThreadPauser.checkPause();
        //System.out.println(threadName + ": Обработка МЕТРИК для заявки " + request.getId());

        // Сохранение данных метрик в базу данных
        saveMetricsToDatabase(request);
        //System.out.println(threadName + ": sleeping for " + (long) Math.exp((double)getProcessedCount()/1000));
        Thread.sleep((long) Math.exp((double) getProcessedCount()/devicePause));
        //Thread.sleep(devicePause + 100);
    }

    // Добавление заявки в очередь обработки
    public void submitRequest(Request request) {
        processingQueue.offer(request);
    }

    // Проверка, свободен ли прибор
    public boolean isAvailable() {
        return !isBusy.get();
    }

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

    public long getBusyTime(){
        return busyTime;
    }

    public Request getCurrentRequest() { return currentRequest; }

    public long getTimeOnDeviceCritical() { return timeOnDeviceCritical; }
    public long getTimeOnDeviceWarning() { return timeOnDeviceWarning; }
    public long getTimeOnDeviceMetrics() { return timeOnDeviceMetrics; }

    public ArrayList<Long> getReqTimeOnDeviceCritical() { return reqTimeOnDeviceCritical; }
    public ArrayList<Long> getReqTimeOnDeviceWarning() { return reqTimeOnDeviceWarning; }
    public ArrayList<Long> getReqTimeOnDeviceMetrics() { return reqTimeOnDeviceMetrics; }

    private void saveToDatabase(Request request) {
        //System.out.println("Device1: Сохранение отчета в БД для заявки " + request.getId());
    }

    private void saveMetricsToDatabase(Request request) {
        //System.out.println("Device3: Сохранение данных метрик в БД для заявки " + request.getId());
    }
}