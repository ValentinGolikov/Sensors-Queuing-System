package Engine;

import Engine.Threads.ThreadPauser;
import Engine.Tracking.ManualModeController;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectionDispatcher implements Runnable {
    private final Buffer buffer;
    private final ManualModeController manualController;
    private Device device1;
    private Device device2;
    private Device device3;
    private final AtomicBoolean running;
    private final LimitedInteger pointer = new LimitedInteger(2);

    private Thread device1Thread;
    private Thread device2Thread;
    private Thread device3Thread;

    public SelectionDispatcher(Buffer buffer) {
        this(buffer, null);
        // Создаем приборы
        this.device1 = new Device("Device1");
        this.device2 = new Device("Device2");
        this.device3 = new Device("Device3");

        // Запускаем потоки приборов
        this.device1Thread = new Thread(device1, "Device1-Thread");
        this.device2Thread = new Thread(device2, "Device2-Thread");
        this.device3Thread = new Thread(device3, "Device3-Thread");
    }

    public SelectionDispatcher(Buffer buffer, ManualModeController manualController) {
        this.buffer = buffer;
        this.manualController = manualController;
        this.running = new AtomicBoolean(true);

        // Создаем приборы
        this.device1 = new Device("Device1");
        this.device2 = new Device("Device2");
        this.device3 = new Device("Device3");

        // Запускаем потоки приборов
        this.device1Thread = new Thread(device1, "Device1-Thread");
        this.device2Thread = new Thread(device2, "Device2-Thread");
        this.device3Thread = new Thread(device3, "Device3-Thread");
    }

    @Override
    public void run() {
        //System.out.println("SelectionDispatcher запущен");

        // Запускаем все приборы
        device1Thread.start();
        device2Thread.start();
        device3Thread.start();

        while (running.get()) {
            try {
                ThreadPauser.checkPause();
                Request request = buffer.getNextRequest(running);
                if (request != null) {
                    dispatchRequest(request);
                }
            } catch (InterruptedException e) {
                System.err.println("SelectionDispatcher прерван: " + e.getMessage());
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Останавливаем приборы
        device1.stop();
        device2.stop();
        device3.stop();

        //System.out.println("SelectionDispatcher завершен");
    }

    private void dispatchRequest(Request request) throws InterruptedException {
        Priority priority = request.getPriority();
        Device selectedDevice = selectDevice(priority);

        if (selectedDevice != null) {
            selectedDevice.submitRequest(request);
        }
    }

    private Device selectDevice(Priority priority) {
        while (true) {
            if (pointer.getValue() == 0) {
                if (device1.isAvailable()) {
                    pointer.increment();
                    return device1;
                }
                pointer.increment();
            }
            else if (pointer.getValue() == 1) {
                if (device2.isAvailable()) {
                    pointer.increment();
                    return device2;
                }
                pointer.increment();
            }
            else if (pointer.getValue() == 2) {
                if (device3.isAvailable()) {
                    pointer.increment();
                    return device3;
                }
                pointer.increment();
            }
        }
    }

    // Методы для получения статистики
    public int getDevice1ProcessedCount() {
        return device1.getProcessedCount();
    }
    public int getDevice2ProcessedCount() {
        return device2.getProcessedCount();
    }
    public int getDevice3ProcessedCount() {
        return device3.getProcessedCount();
    }

    public Request getDevice1CurrentRequest() { return device1.getCurrentRequest(); }
    public Request getDevice2CurrentRequest() { return device2.getCurrentRequest(); }
    public Request getDevice3CurrentRequest() { return device3.getCurrentRequest(); }

    public long getServTimeCritial() {
        return device1.getTimeOnDeviceCritical() + device2.getTimeOnDeviceCritical() +
                device3.getTimeOnDeviceCritical();
    }

    public long getServTimeWarning() {
        return device1.getTimeOnDeviceWarning() + device2.getTimeOnDeviceWarning() +
                device3.getTimeOnDeviceWarning();
    }

    public long getServTimeMetrics() {
        return device1.getTimeOnDeviceMetrics() + device2.getTimeOnDeviceMetrics() +
                device3.getTimeOnDeviceMetrics();
    }

    public ArrayList<Long> getArrayServTimeCritical() {
        ArrayList<Long> onDeviceTime = new ArrayList<>();
        onDeviceTime.addAll(device1.getReqTimeOnDeviceCritical());
        onDeviceTime.addAll(device2.getReqTimeOnDeviceCritical());
        onDeviceTime.addAll(device3.getReqTimeOnDeviceCritical());
        return onDeviceTime;
    }

    public ArrayList<Long> getArrayServTimeWarning() {
        ArrayList<Long> onDeviceTime = new ArrayList<>();
        onDeviceTime.addAll(device1.getReqTimeOnDeviceWarning());
        onDeviceTime.addAll(device2.getReqTimeOnDeviceWarning());
        onDeviceTime.addAll(device3.getReqTimeOnDeviceWarning());
        return onDeviceTime;
    }

    public ArrayList<Long> getArrayServTimeMetrics() {
        ArrayList<Long> onDeviceTime = new ArrayList<>();
        onDeviceTime.addAll(device1.getReqTimeOnDeviceMetrics());
        onDeviceTime.addAll(device2.getReqTimeOnDeviceMetrics());
        onDeviceTime.addAll(device3.getReqTimeOnDeviceMetrics());
        return onDeviceTime;
    }

    public int getProcessedCount(int num) {
        switch(num) {
            case 0 -> { return device1.getProcessedCount(); }
            case 1 -> { return device2.getProcessedCount(); }
            case 2 -> { return device3.getProcessedCount(); }
            default -> { return -1; }
        }
    }

    public long getBusyTime(int num) {
        switch(num) {
            case 0 -> { return device1.getBusyTime(); }
            case 1 -> { return device2.getBusyTime(); }
            case 2 -> { return device3.getBusyTime(); }
            default -> { return -1; }
        }
    }


    public void stop() {
        running.set(false);

        // Также останавливаем устройства
        device1.stop();
        device2.stop();
        device3.stop();
    }

}