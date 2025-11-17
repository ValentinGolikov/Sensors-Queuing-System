package Engine.Tracking;

import Engine.Devices.Device;
import Engine.SelectionDispatcher;
import Engine.Buffer;
import Engine.Request;
import Engine.Priority;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ManualModeController {
    private final AtomicBoolean paused = new AtomicBoolean(true);
    private final AtomicBoolean stepExecuted = new AtomicBoolean(false);
    private final Scanner scanner = new Scanner(System.in);

    public void waitForStep() {
        if (paused.get()) {
            System.out.println("\n⏸️  РУЧНОЙ РЕЖИМ - Нажмите Enter для следующего шага...");
            scanner.nextLine();
        }
    }

    public void setPaused(boolean paused) {
        this.paused.set(paused);
        if (!paused) {
            System.out.println("▶️  Система продолжает работу...");
        } else {
            System.out.println("⏸️  Система приостановлена");
        }
    }

    public void displaySystemState(Buffer buffer, SelectionDispatcher dispatcher) {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("📊 СИСТЕМНЫЙ РАЗРЕЗ - Состояние системы");
        System.out.println("=".repeat(100));

        // Статистика системы
        displaySystemStatistics(buffer, dispatcher);

        // Таблица активных заявок
        displayRequestsTable();

        // Состояние приборов
        displayDevicesState(dispatcher);

        System.out.println("=".repeat(100));
    }

    private void displaySystemStatistics(Buffer buffer, SelectionDispatcher dispatcher) {
        System.out.println("📈 СТАТИСТИКА СИСТЕМЫ:");
        System.out.printf("   • Заявок в буфере: %d\n", buffer.getCurrentSize());
        System.out.printf("   • Всего обработано: %d\n", RequestTracker.getTotalProcessed());
        System.out.printf("   • Активных заявок: %d\n", RequestTracker.getActiveRequests().size());

        Map<Integer, RequestTracker.RequestInfo> requests = RequestTracker.getActiveRequests();
        long avgLifeTime = (long) requests.values().stream()
                .mapToLong(info -> info.getLifeTime().toMillis())
                .average()
                .orElse(0);
        System.out.printf("   • Среднее время жизни: %.2f сек\n", avgLifeTime / 1000.0);
    }

    private void displayRequestsTable() {
        System.out.println("\n📋 ТАБЛИЦА ЗАЯВОК:");
        System.out.println("─".repeat(80));
        System.out.printf("│ %-12s │ %-10s │ %-12s │ %-15s │ %-13s │\n",
                "ID", "Приоритет", "Статус", "Время жизни (сек)", "Прибор");
        System.out.println("─".repeat(80));

        Map<Integer, RequestTracker.RequestInfo> requests = RequestTracker.getActiveRequests();
        if (requests.isEmpty()) {
            System.out.printf("│ %-76s │\n", "Нет активных заявок");
        } else {
            requests.values().forEach(info -> {
                String lifeTime = String.format("%.2f", info.getLifeTime().toMillis() / 1000.0);
                String device = info.getCurrentDevice() != null ? info.getCurrentDevice() : "—";
                System.out.printf("│ %-12s │ %-10s │ %-12s │ %-17s │ %-20s │\n",
                        info.getId(),
                        info.getPriority(),
                        info.getStatus(),
                        lifeTime,
                        device);
            });
        }
        System.out.println("─".repeat(80));
    }

    private void displayDevicesState(SelectionDispatcher dispatcher) {
        System.out.println("\n⚙️  СОСТОЯНИЕ ПРИБОРОВ:");
        System.out.println("─".repeat(60));
        System.out.printf("│ %-10s │ %-8s │ %-12s │ %-20s │\n",
                "Прибор", "Статус", "Обработано", "Текущая заявка");
        System.out.println("─".repeat(60));

        // Здесь нужно добавить методы для получения состояния приборов из SelectionDispatcher
        displayDeviceState("Device1", dispatcher.getDevice1ProcessedCount(), dispatcher.getDevice1CurrentRequest());
        displayDeviceState("Device2", dispatcher.getDevice2ProcessedCount(), dispatcher.getDevice2CurrentRequest());
        displayDeviceState("Device3", dispatcher.getDevice3ProcessedCount(), dispatcher.getDevice3CurrentRequest());

        System.out.println("─".repeat(60));
    }

    private void displayDeviceState(String name, int processedCount, Request currentRequest) {
        String status = currentRequest != null ? "ЗАНЯТ" : "СВОБОДЕН";
        int currentReqId = currentRequest != null ? currentRequest.getId() : -1;
        System.out.printf("│ %-10s │ %-8s │ %-12d │ %-20s │\n",
                name, status, processedCount, currentReqId);
    }
}