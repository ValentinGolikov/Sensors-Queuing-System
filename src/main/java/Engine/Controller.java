package Engine;

import Engine.Threads.RequestsGenerator;
import Engine.Threads.ThreadPauser;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Controller implements Runnable {
    private final BlockingQueue<DataPack> dataPackQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Request> requestsQueue = new LinkedBlockingQueue<>();
    private volatile boolean isRunning = true;

    /**
     * Метод для приема DataPack от потоков-генераторов
     */
    public void submitDataPack(DataPack dataPack) throws InterruptedException {
        if (!isRunning) {
            throw new IllegalStateException("Controller is not running");
        }
        dataPackQueue.put(dataPack);
    }

    /**
     * Основной цикл обработки Controller
     */
    @Override
    public void run() {
        System.out.println("Controller started");

        while (isRunning) {
            if (RequestsGenerator.isStopped() && dataPackQueue.isEmpty()) {
                isRunning = false;
            }
            else {
                try {
                    ThreadPauser.checkPause();
                    if (!dataPackQueue.isEmpty()) {
                        DataPack dataPack = dataPackQueue.take();

                    Request request = convertToRequest(dataPack);

                    requestsQueue.put(request);
                    }

                } catch (InterruptedException e) {
                    System.out.println("Controller interrupted");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in Controller: " + e.getMessage());
                }
            }
        }

        System.out.println("Controller stopped");
    }

    /**
     * Преобразует DataPack в Request с определением приоритета
     */
    public Request convertToRequest(DataPack dataPack) {
        Priority priority = determinePriority(dataPack.getMetrics());

        Request request = new Request(priority);

        request.setData(String.format("%s; %s; %s;\nData:\n%s",
                dataPack.getTrainNumber(),
                dataPack.getTrainCarriageNumber(),
                dataPack.getWheelNumber(),
                dataPack.getMetrics()
        ));

        return request;
    }

    /**
     * Определяет приоритет на основе метрик DataPack
     */
    private Priority determinePriority(String metrics) {
        if (metrics.contains("status:CRITICAL")) {
            return Priority.CRITICAL;
        } else if (metrics.contains("status:WARNING")) {
            return Priority.WARNING;
        } else {
            return Priority.METRICS;
        }
    }

    /**
     * Геттер для очереди Request (для ReceptionDispatcher)
     */
    public BlockingQueue<Request> getRequestsQueue() {
        return requestsQueue;
    }

    public void stop() {
        isRunning = false;
        System.out.println("Controller: получена команда остановки");
    }
}