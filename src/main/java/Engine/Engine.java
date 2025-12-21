package Engine;

import Engine.Threads.RequestsGenerator;
import Engine.Threads.ThreadPauser;
import Engine.Tracking.ManualModeController;
import Engine.Tracking.RequestTracker;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Engine {
    private static ManualModeController manualController;
    private static boolean manualMode = false;

    public static void main(String[] args) {
        // Проверяем аргументы командной строки
        if (args.length > 0 && args[0].equals("--manual")) {
            manualMode = true;
            System.out.println("=== РЕЖИМ РУЧНОГО УПРАВЛЕНИЯ ===");
            manualController = new ManualModeController();
        } else {
            System.out.println("=== АВТОМАТИЧЕСКИЙ РЕЖИМ ===");
        }

        Buffer buf = new Buffer(10);
        Controller controller = new Controller();

        // Создаем компоненты в зависимости от режима
        RequestsGenerator requestsGenerator;
        SelectionDispatcher selectionDispatcher;

        if (manualMode) {
            requestsGenerator = new RequestsGenerator(controller);
            selectionDispatcher = new SelectionDispatcher(buf, manualController);
        } else {
            requestsGenerator = new RequestsGenerator(controller);
            selectionDispatcher = new SelectionDispatcher(buf);
        }

        ReceptionDispatcher receptionDispatcher = new ReceptionDispatcher(controller, buf);

        Thread receptionDispatcherThread = new Thread(receptionDispatcher, "receptionDispatcherThread");
        Thread controllerThread = new Thread(controller, "controllerThread");
        Thread requestsGeneratorThread = new Thread(requestsGenerator, "requestsGeneratorThread");
        Thread selectionDispatcherThread = new Thread(selectionDispatcher, "selectionDispatcherThread");

        if (manualMode) {
            // В РУЧНОМ РЕЖИМЕ: сначала показываем инструкции, потом запускаем потоки
            try {
                runManualMode(buf, selectionDispatcher, requestsGenerator,
                        receptionDispatcherThread, controllerThread,
                        requestsGeneratorThread, selectionDispatcherThread, controller, receptionDispatcher);
            } catch (InterruptedException e) {
                System.err.println(e);
            }

        } else {
            System.out.println("\nСИСТЕМА В АВТОМАТИЧЕСКОМ РЕЖИМЕ");
            System.out.println("\nВведите q для выхода");
            AtomicBoolean running = new AtomicBoolean(true);
            Thread keyboardListenerThread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (running.get()) {
                    try {
                        Thread.sleep(20000);
                        running.set(false);
                        // Останавливаем все компоненты
                        stopAllComponents(requestsGenerator, selectionDispatcher,
                                controller, receptionDispatcher, buf);
                        scanner.close();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "keyboardListenerThread");
            keyboardListenerThread.setDaemon(true);
            keyboardListenerThread.start();

            // Запускаем рабочие потоки
            controllerThread.start();
            receptionDispatcherThread.start();
            requestsGeneratorThread.start();
            selectionDispatcherThread.start();

            // Ждем завершения всех потоков
            try {
                controllerThread.join();
                receptionDispatcherThread.join();
                requestsGeneratorThread.join();
                selectionDispatcherThread.join();
                keyboardListenerThread.join(1000); // Ждем завершения слушателя
                System.out.println("Всего отказов: " + buf.getTotalRejected());
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }
        printStatistic(requestsGenerator, selectionDispatcher, buf);
    }

    private static void runManualMode(Buffer buffer, SelectionDispatcher selectionDispatcher,
                                      RequestsGenerator requestsGenerator,
                                      Thread receptionDispatcherThread, Thread controllerThread,
                                      Thread requestsGeneratorThread, Thread selectionDispatcherThread,
                                      Controller controller, ReceptionDispatcher receptionDispatcher) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        boolean systemStarted = false;
        boolean running = true;

        while (running) {
            System.out.println("\nСИСТЕМА В РУЧНОМ РЕЖИМЕ");
            System.out.println("Команды:");
            System.out.println("  [Enter] - следующий шаг");
            System.out.println("  'start' + Enter - запустить все потоки");
            System.out.println("  'q' + Enter - выход");
            System.out.println("=" .repeat(50));
            System.out.print("\nВведите команду: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "":
                    if (systemStarted) {
                        ThreadPauser.resumeAllThreads();
                        Thread.sleep(100);
                        ThreadPauser.pauseAllThreads();
                        manualController.displaySystemState(buffer, selectionDispatcher, requestsGenerator);
                    } else {
                        System.out.println("Сначала запустите систему командой 'start'");
                    }
                    break;

                case "start":
                    if (!systemStarted) {
                        System.out.println("ЗАПУСК ВСЕХ ПОТОКОВ...");
                        controllerThread.start();
                        receptionDispatcherThread.start();
                        requestsGeneratorThread.start();
                        selectionDispatcherThread.start();
                        systemStarted = true;

                        ThreadPauser.pauseAllThreads();
                        manualController.displaySystemState(buffer, selectionDispatcher, requestsGenerator);
                    } else {
                        System.out.println("Система уже запущена");
                    }
                    break;

                case "q":
                case "Q":
                    running = false;
                    System.out.println("Завершение работы...");
                    break;

                default:
                    System.out.println("Неизвестная команда. Доступные команды: [Enter], 'start', 'q'");
                    break;
            }
        }

        // Останавливаем систему если она была запущена
        if (systemStarted) {
            stopAllComponents(requestsGenerator, selectionDispatcher, controller,
                    receptionDispatcher, buffer);
        }
        scanner.close();
    }

    private static void stopAllComponents(RequestsGenerator requestsGenerator, SelectionDispatcher selectionDispatcher,
                                          Controller controller, ReceptionDispatcher receptionDispatcher, Buffer buffer) {
        System.out.println("\nОСТАНОВКА СИСТЕМЫ...");

        // Останавливаем компоненты в правильном порядке
        if (requestsGenerator != null) {
            requestsGenerator.stop();
        }
        if (selectionDispatcher != null) {
            selectionDispatcher.stop();
        }
        if (controller != null) {
            controller.stop();
        }
        if (receptionDispatcher != null) {
            receptionDispatcher.stop();
        }
    }

    private static int getSourceTypeFromPriority(Priority priority) {
        switch (priority) {
            case CRITICAL: return 1;
            case WARNING: return 2;
            case METRICS: return 3;
            default: return 0;
        }
    }
    private static void printStatistic(RequestsGenerator requestsGenerator,
                                       SelectionDispatcher selectionDispatcher,
                                       Buffer buffer) {
        // Статистика по источникам
        String[] sources = {"1 (Critical)", "2 (Warning) ", "3 (Metrics) "};
        int[] generated = {
                requestsGenerator.getCriticalGenerated(),
                requestsGenerator.getWarningGenerated(),
                requestsGenerator.getMetricsGenerated()
        };
        int[] rejected = {
                buffer.getCriticalRejected(),
                buffer.getWarningRejected(),
                buffer.getMetricsRejected()
        };

        // Финальная статистика
        System.out.println("\nФИНАЛЬНАЯ СТАТИСТИКА:\n");

        System.out.println("╔═════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║     Src      │  Gen  │ Rej(%) │  T_sys  │  T_wait  │  T_serv  │  D_wait  │  D_serv  ║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════════════════════╣");

        for (int i = 0; i < sources.length; i++) {
            int countCritical = buffer.getCountCritical();
            int countWarning = buffer.getCountWarning();
            int countMetrics = buffer.getCountMetrics();
            double percentRejected = generated[i] > 0 ? (double) rejected[i] / generated[i] * 100 : 0;
            double avgWaitTime = 0.0;
            double avgTimeInSystem = 0.0;
            double avgServiceTime = 0.0;
            double dispWait = 0.0;
            double dispServ = 0.0;
            switch (i){
                case 0 -> {
                    avgWaitTime = countCritical > 0 ?
                        (double) buffer.getFullTimeInBufferCritical() / countCritical : 0;
                    avgServiceTime = countCritical > 0 ?
                            (double) selectionDispatcher.getServTimeCritial() / countCritical : 0;
                    avgTimeInSystem = avgServiceTime + avgWaitTime;

                    double finalAvgWaitTime = avgWaitTime;
                    double finalAvgServiceTime = avgServiceTime;
                    long sum = buffer.getReqTimeInBufferCritical().stream()
                            .mapToLong(time -> (long) Math.pow(time - finalAvgWaitTime, 2))
                            .sum();
                    dispWait = ((double) sum/(countMetrics));

                    long summ = selectionDispatcher.getArrayServTimeCritical().stream()
                            .mapToLong(time -> (long) Math.pow(time - finalAvgServiceTime, 2))
                            .sum();
                    dispServ = ((double) summ/(countMetrics));
                }
                case 1 -> {
                    avgWaitTime = countWarning > 0 ?
                        (double) buffer.getFullTimeInBufferWarning() / countWarning : 0;
                    avgServiceTime = countWarning > 0 ?
                            (double) selectionDispatcher.getServTimeWarning() / countWarning : 0;
                    avgTimeInSystem = avgServiceTime + avgWaitTime;

                    double finalAvgWaitTime = avgWaitTime;
                    double finalAvgServiceTime = avgServiceTime;
                    long sum = buffer.getReqTimeInBufferWarning().stream()
                            .mapToLong(time -> (long) Math.pow(time - finalAvgWaitTime, 2))
                            .sum();
                    dispWait = ((double) sum/(countMetrics));

                    long summ = selectionDispatcher.getArrayServTimeWarning().stream()
                            .mapToLong(time -> (long) Math.pow(time - finalAvgServiceTime, 2))
                            .sum();
                    dispServ = ((double) summ/(countMetrics));
                }
                case 2 -> {
                    avgWaitTime = countMetrics > 0 ?
                        (double) buffer.getFullTimeInBufferMetrics() / countMetrics : 0;
                    avgServiceTime = countMetrics > 0 ?
                            (double) selectionDispatcher.getServTimeMetrics() / countMetrics : 0;
                    avgTimeInSystem = avgServiceTime + avgWaitTime;

                    double finalAvgWaitTime = avgWaitTime;
                    double finalAvgServiceTime = avgServiceTime;
                    long sum = buffer.getReqTimeInBufferMetrics().stream()
                            .mapToLong(time -> (long) Math.pow(time - finalAvgWaitTime, 2))
                            .sum();
                    dispWait = ((double) sum/(countMetrics));

                    long summ = selectionDispatcher.getArrayServTimeMetrics().stream()
                            .mapToLong(time -> (long) Math.pow(time - finalAvgServiceTime, 2))
                            .sum();
                    dispServ = ((double) summ/(countMetrics));
                }
            }

            System.out.printf("║ %-10s │ %-6d│ %-6.2f │ %-8.2f│ %-9.2f│ %-9.2f│ %-9.2f│ %-9.2f║%n",
                    sources[i],
                    generated[i],
                    percentRejected,
                    avgTimeInSystem,
                    avgWaitTime,       // переводим в секунды
                    avgServiceTime,    // переводим в секунды
                    dispWait,  // D_wait - пока не трогаем
                    dispServ  // D_serv - пока не трогаем
            );
        }

        System.out.println("╚═════════════════════════════════════════════════════════════════════════════════════╝");

        //System.out.println("Всего обработано заявок: " + RequestTracker.getTotalProcessed());
        System.out.println("=== СИСТЕМА ЗАВЕРШИЛА РАБОТУ ===");
    }



}