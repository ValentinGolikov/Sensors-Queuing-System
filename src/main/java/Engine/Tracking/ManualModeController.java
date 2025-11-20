package Engine.Tracking;

import Engine.*;
import Engine.Devices.Device;
import Engine.Threads.CriticalGenerator;
import Engine.Threads.RequestsGenerator;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ManualModeController {
    private final AtomicBoolean paused = new AtomicBoolean(true);
    private final AtomicBoolean stepExecuted = new AtomicBoolean(false);
    private final Scanner scanner = new Scanner(System.in);

    public void setPaused(boolean paused) {
        this.paused.set(paused);
        if (!paused) {
            System.out.println("▶️  Система продолжает работу...");
        } else {
            System.out.println("⏸️  Система приостановлена");
        }
    }

    public void displaySystemState(Buffer buffer, SelectionDispatcher dispatcher, RequestsGenerator requestsGenerator) {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("📊 СИСТЕМНЫЙ РАЗРЕЗ - Состояние системы");
        System.out.println("=".repeat(100));

        // Статистика системы
        displaySystemStatistics(buffer, dispatcher);

        // Таблица активных заявок
        displayRequestsTable();

        //Буфер
        displayBufferTable(buffer);

        //Источники
        displaySourcesStatistics(requestsGenerator, buffer);

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
        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                ТАБЛИЦА ЗАЯВОК                                    ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║     ID     │ Приоритет │   Статус   │ Время в системе (сек) │       Прибор       ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════════════════════╣");

        Map<Integer, RequestTracker.RequestInfo> requests = RequestTracker.getActiveRequests();
        if (requests.isEmpty()) {
            System.out.println("║                            Нет активных заявок                                   ║");
        } else {
            requests.values().forEach(info -> {
                String lifeTime = String.format("%.2f", calculateTimeInSystem(info) / 1000.0);
                String device = info.getCurrentDevice() != null ? info.getCurrentDevice() : "—";
                System.out.printf("║ %-10s │ %-9s │ %-10s │ %-21s │ %-18s ║%n",
                        info.getId(),
                        info.getPriority(),
                        info.getStatus(),
                        lifeTime,
                        device);
            });
        }
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════════╝");
    }

    private void displayDevicesState(SelectionDispatcher dispatcher) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                     СОСТОЯНИЕ ПРИБОРОВ                       ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  Прибор  │ Статус   │ Обработано │     Текущая заявка        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");

        // Здесь нужно добавить методы для получения состояния приборов из SelectionDispatcher
        displayDeviceState("Device1", dispatcher.getDevice1ProcessedCount(), dispatcher.getDevice1CurrentRequest());
        displayDeviceState("Device2", dispatcher.getDevice2ProcessedCount(), dispatcher.getDevice2CurrentRequest());
        displayDeviceState("Device3", dispatcher.getDevice3ProcessedCount(), dispatcher.getDevice3CurrentRequest());

        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    private void displayDeviceState(String name, int processedCount, Request currentRequest) {
        String status = currentRequest != null ? "ЗАНЯТ" : "СВОБОДЕН";
        int currentReqId = currentRequest != null ? currentRequest.getId() : -1;
        System.out.printf("║ %-8s │ %-8s │ %-10d │  %-23s  ║%n",
                name, status, processedCount, currentReqId);
    }

    private void displayBufferTable(Buffer buffer) {
        int bufferSize = buffer.getSize();
        int currentPointer = buffer.getPtr().getValue();

        System.out.println("\n╔═════════════════════ БУФЕР ════════════════════╗");

        // Строка с номерами ячеек
        System.out.print("║ Яч: ");
        for (int i = 0; i < bufferSize; i++) {
            System.out.printf("│ " + i + " ");
        }
        System.out.println("│  ║");

        // Строка с ID заявок
        System.out.print("║ ID: ");
        for (int i = 0; i < bufferSize; i++) {
            Request request = buffer.getRequest(i);
            String requestId = (request != null) ? String.format("%3d", request.getId()) : " - ";
            System.out.printf("│%-3s", requestId);
        }
        System.out.println("│  ║");

        // Строка с указателями
        System.out.print("║ Ук: ");
        for (int i = 0; i < bufferSize; i++) {
            String pointerSymbol = (i == currentPointer) ? " ↑ " : " × ";
            System.out.printf("│%3s", pointerSymbol);
        }
        System.out.println("│  ║");

        System.out.println("╚════════════════════════════════════════════════╝");
    }

    private void displaySourcesStatistics(RequestsGenerator requestsGenerator, Buffer buffer) {
        List<String> generators = List.of("Источник1", "Источник2", "Источник3");

        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║     СТАТИСТИКА ПО ИСТОЧНИКАМ      ║");
        System.out.println("╠═══════════════════════════════════╣");
        System.out.println("║ Источник │ Сгенерировано │ Отказы ║");
        System.out.println("╠═══════════════════════════════════╣");


        for (int i = 0; i < generators.size(); i++) {
            String generator = generators.get(i);
            switch (generator) {
                case "Источник1":
                System.out.printf("║    %2d    │     %3d       │  %3d   ║%n",
                        i + 1,
                        requestsGenerator.getCriticalGenerated(),
                        buffer.getCriticalRejected());
                break;
                case "Источник2":
                    System.out.printf("║    %2d    │     %3d       │  %3d   ║%n",
                            i + 1,
                            requestsGenerator.getWarningGenerated(),
                            buffer.getWarningRejected());
                    break;
                case "Источник3":
                    System.out.printf("║    %2d    │     %3d       │  %3d   ║%n",
                            i + 1,
                            requestsGenerator.getMetricsGenerated(),
                            buffer.getMetricsRejected());
                    break;
            }
        }
        System.out.println("║──────────│───────────────│────────║");

        System.out.println("╚═══════════════════════════════════╝");
    }

    private long calculateTimeInSystem(RequestTracker.RequestInfo info) {
        if (info.getProcessedTime() != null && info.getCreationTime() != null) {
            return Duration.between(info.getProcessedTime(), info.getCreationTime()).toMillis() -
                    PauseTimeManager.getPauseDuration();
        }
        return Duration.ZERO.toMillis();
    }
}