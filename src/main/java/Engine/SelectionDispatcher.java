package Engine;

import Engine.Threads.ThreadPauser;
import Engine.Tracking.ManualModeController;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SelectionDispatcher implements Runnable {
    private final Buffer buffer;
    private final ManualModeController manualController;
    private final List<Device> devices;
    private final List<Thread> deviceThreads;
    private final AtomicBoolean running;
    private final LimitedInteger pointer;

    private long pause;

    private int total = 0;

    // Конструктор с динамическим количеством устройств
    public SelectionDispatcher(Buffer buffer, int deviceCount, int devicePause) {
        this(buffer, deviceCount, null, devicePause);
    }

    public SelectionDispatcher(Buffer buffer, int deviceCount, ManualModeController manualController, int devicePause) {
        this.buffer = buffer;
        this.manualController = manualController;
        this.running = new AtomicBoolean(true);

        // Создаем приборы (количество определяется параметром deviceCount)
        this.devices = new ArrayList<>(deviceCount);
        this.deviceThreads = new ArrayList<>(deviceCount);
        this.pointer = new LimitedInteger(deviceCount - 1); // Устанавливаем максимальное значение

        // Создаем указанное количество устройств
        for (int i = 0; i < deviceCount; i++) {
            String deviceName = "Device" + (i + 1);
            Device device = new Device(deviceName, devicePause);
            devices.add(device);

            Thread deviceThread = new Thread(device, deviceName + "-Thread");
            deviceThreads.add(deviceThread);
        }
    }

    @Override
    public void run() {
        //System.out.println("SelectionDispatcher запущен. Количество устройств: " + devices.size());

        // Запускаем все потоки приборов
        for (Thread thread : deviceThreads) {
            thread.start();
        }

        while (running.get()) {
            try {
                ThreadPauser.checkPause();
                Request request = buffer.getNextRequest(running);
                if (request != null) {
                    //total++;
                    dispatchRequest(request);
                }
            } catch (InterruptedException e) {
                System.err.println("SelectionDispatcher прерван: " + e.getMessage());
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Останавливаем все приборы
        for (Device device : devices) {
            device.stop();
        }

        //System.out.println("SelectionDispatcher завершен");
    }

    private void dispatchRequest(Request request) throws InterruptedException {
        DateTime start = new DateTime();
        Device selectedDevice = selectDevice();
        pause += start.getDifferenceFromNow();
        total++;

        selectedDevice.submitRequest(request);
    }

    private Device selectDevice() {
        while (true) {
            Device device = devices.get(pointer.getValue());
            if (device.isAvailable()) {
                pointer.increment();
                return device;
            }
            pointer.increment();
        }
    }

    // Методы для получения статистики
    public int getDeviceProcessedCount(int deviceIndex) {
        if (deviceIndex >= 0 && deviceIndex < devices.size()) {
            return devices.get(deviceIndex).getProcessedCount();
        }
        return -1;
    }

    public int getDeviceCount() {
        return devices.size();
    }

    public Request getDeviceCurrentRequest(int deviceIndex) {
        if (deviceIndex >= 0 && deviceIndex < devices.size()) {
            return devices.get(deviceIndex).getCurrentRequest();
        }
        return null;
    }

    public long getServTimeCritical() {
        long totalTime = 0;
        for (Device device : devices) {
            totalTime += device.getTimeOnDeviceCritical();
        }
        return totalTime;
    }

    public long getServTimeWarning() {
        long totalTime = 0;
        for (Device device : devices) {
            totalTime += device.getTimeOnDeviceWarning();
        }
        return totalTime;
    }

    public long getServTimeMetrics() {
        long totalTime = 0;
        for (Device device : devices) {
            totalTime += device.getTimeOnDeviceMetrics();
        }
        return totalTime;
    }

    public ArrayList<Long> getArrayServTimeCritical() {
        ArrayList<Long> onDeviceTime = new ArrayList<>();
        for (Device device : devices) {
            onDeviceTime.addAll(device.getReqTimeOnDeviceCritical());
        }
        return onDeviceTime;
    }

    public ArrayList<Long> getArrayServTimeWarning() {
        ArrayList<Long> onDeviceTime = new ArrayList<>();
        for (Device device : devices) {
            onDeviceTime.addAll(device.getReqTimeOnDeviceWarning());
        }
        return onDeviceTime;
    }

    public ArrayList<Long> getArrayServTimeMetrics() {
        ArrayList<Long> onDeviceTime = new ArrayList<>();
        for (Device device : devices) {
            onDeviceTime.addAll(device.getReqTimeOnDeviceMetrics());
        }
        return onDeviceTime;
    }

    public int getProcessedCount(int deviceIndex) {
        return getDeviceProcessedCount(deviceIndex);
    }

    public long getBusyTime(int deviceIndex) {
        if (deviceIndex >= 0 && deviceIndex < devices.size()) {
            return devices.get(deviceIndex).getBusyTime();
        }
        return -1;
    }

    public int getTotal() {
        return total;
    }

    public void stop() {
        running.set(false);

        // Останавливаем все устройства
        for (Device device : devices) {
            device.stop();
        }
    }

    // Дополнительные методы для удобства
    public Device getDevice(int index) {
        if (index >= 0 && index < devices.size()) {
            return devices.get(index);
        }
        return null;
    }

    public String getDeviceName(int index) {
        Device device = getDevice(index);
        return device != null ? device.getName() : "";
    }

    public long getPause() {
        return pause/total;
    }
}