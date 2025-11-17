package Engine;

import Engine.Devices.Device;
import Engine.Devices.Device1;
import Engine.Devices.Device2;
import Engine.Devices.Device3;
<<<<<<< HEAD
=======
import Engine.Threads.ThreadPauser;
import Engine.Tracking.ManualModeController;
>>>>>>> 148240b (auto_mode_v0.3.1)

import java.util.concurrent.atomic.AtomicBoolean;

public class SelectionDispatcher implements Runnable {
    private final Buffer buffer;
    private final Device device1;
    private final Device device2;
    private final Device device3;
    private final AtomicBoolean running;

    private final Thread device1Thread;
    private final Thread device2Thread;
    private final Thread device3Thread;

    public SelectionDispatcher(Buffer buffer) {
        this.buffer = buffer;
        this.running = new AtomicBoolean(true);

        // Создаем приборы
        this.device1 = new Device1();
        this.device2 = new Device2();
        this.device3 = new Device3();

        // Запускаем потоки приборов
        this.device1Thread = new Thread(device1, "Device1-Thread");
        this.device2Thread = new Thread(device2, "Device2-Thread");
        this.device3Thread = new Thread(device3, "Device3-Thread");
    }

    @Override
    public void run() {
        System.out.println("SelectionDispatcher запущен");

        // Запускаем все приборы
        device1Thread.start();
        device2Thread.start();
        device3Thread.start();

        while (running.get()) {
            try {
<<<<<<< HEAD
=======
                ThreadPauser.checkPause();
>>>>>>> 148240b (auto_mode_v0.3.1)
                Request request = buffer.getNextRequest();
                if (request != null) {
                    dispatchRequest(request);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Останавливаем приборы
        device1.stop();
        device2.stop();
        device3.stop();

        System.out.println("SelectionDispatcher завершен");
    }

    private void dispatchRequest(Request request) throws InterruptedException {
        Priority priority = request.getPriority();
        Device selectedDevice = selectDevice(priority);

        if (selectedDevice != null) {
            System.out.println("SelectionDispatcher: Направляю заявку " + request.getId() +
                    " с приоритетом " + priority + " на " + selectedDevice.getName());
            selectedDevice.submitRequest(request);
        } else {
            System.out.println("SelectionDispatcher: Не найден свободный прибор для заявки " + request.getId());
            // Можно добавить логику повторной попытки или отложенной обработки
        }
    }

    private Device selectDevice(Priority priority) throws InterruptedException {
        switch (priority) {
            case CRITICAL:
                // Критические заявки всегда на Device1
                return waitForDevice(device1, priority);

            case WARNING:
                // Предупреждения: сначала Device2, если занят - Device1
                if (device2.isAvailable() && device2.canHandle(priority)) {
                    return device2;
                } else {
                    return waitForDevice(device1, priority);
                }

            case METRICS:
                // Метрики только на Device3
                return waitForDevice(device3, priority);

            default:
                return null;
        }
    }

    private Device waitForDevice(Device device, Priority priority) throws InterruptedException {
        // Ждем пока прибор освободится и сможет обработать заявку
        while (!device.isAvailable() || !device.canHandle(priority)) {
            Thread.sleep(100); // Краткая пауза перед повторной проверкой
        }
        return device;
    }

    public void stop() {
        running.set(false);
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
}