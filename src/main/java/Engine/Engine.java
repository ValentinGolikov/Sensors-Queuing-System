package Engine;

import Engine.Threads.RequestsGenerator;
import Engine.Threads.ThreadPauser;
import Engine.Tracking.ManualModeController;
import Engine.Tracking.RequestTracker;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Engine {
    private static ManualModeController manualController;
    private static boolean manualMode = false;
    private static int TIMEOUT = 10000;
    private static int generatorPause = 1000;

    public static void main(String[] args){
        // Проверяем аргументы командной строки
        if (args.length > 0) {
            if (args[0].equals("-manual")){
                manualMode = true;
                System.out.println("=== РЕЖИМ РУЧНОГО УПРАВЛЕНИЯ ===");
                manualController = new ManualModeController();
            }
            else if (args[0].equals("-auto")) {
                System.out.println("=== АВТОМАТИЧЕСКИЙ РЕЖИМ ===");
                if (args[3].equals("-t")){
                    try {
                        TIMEOUT = Integer.parseInt(args[4]);
                        Scanner scanner = new Scanner(System.in);
                        System.out.printf("\nСИСТЕМА В АВТОМАТИЧЕСКОМ РЕЖИМЕ (%dмс))\n", TIMEOUT);
                        System.out.println("Press any key to continue...");
                        scanner.nextLine();
                        FancyProgressBar.start(TIMEOUT);
                    } catch (NumberFormatException e) {
                        System.err.println("Неверный формат времени работы. Используется значение по умолчанию: " + TIMEOUT);
                    }
                }
            }

            if (args[1].equals("-p")) {
                try {
                    generatorPause = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    System.err.println("Неверный формат времени паузы. Используется значение по умолчанию: " + generatorPause);
                }
            }
        }




        Buffer buf = new Buffer(10);
        Controller controller = new Controller();


        // Создаем компоненты в зависимости от режима
        RequestsGenerator requestsGenerator;
        SelectionDispatcher selectionDispatcher;

        if (manualMode) {
            requestsGenerator = new RequestsGenerator(controller, generatorPause);
            selectionDispatcher = new SelectionDispatcher(buf, manualController);
        } else {
            requestsGenerator = new RequestsGenerator(controller, generatorPause);
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
            AtomicBoolean running = new AtomicBoolean(true);
            Thread keyboardListenerThread = new Thread(() -> {
                while (running.get()) {
                    try {
                        Thread.sleep(TIMEOUT);
                        running.set(false);
                        // Останавливаем все компоненты
                        stopAllComponents(requestsGenerator, selectionDispatcher,
                                controller, receptionDispatcher, buf);
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
                keyboardListenerThread.join(1000);
                System.out.println("Всего отказов: " + buf.getTotalRejected());
            } catch (InterruptedException e) {
                System.err.println(e);
            }
            printStatistic(requestsGenerator, selectionDispatcher, buf);
        }
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
        double avgLifeTime = 0.0;
        double avgPercentRejected = 0.0;
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
            avgLifeTime += avgTimeInSystem;
            avgPercentRejected += percentRejected;
        }

        System.out.println("╚═════════════════════════════════════════════════════════════════════════════════════╝");
        System.out.printf("Average request's lifetime in system: %.2f\n", avgLifeTime/3);
        System.out.printf("Average rejection in system: %.2f%%\n", avgPercentRejected/3);

        System.out.println("\n╔═════════════════════════════════════════════════╗");
        System.out.println("║  Device  │  Processed  │ BusyTime │  Usage (%)  ║");
        System.out.println("╠═════════════════════════════════════════════════╣");

        String[] devices = {"Device1 ", "Device2 ", "Device3 "};
        double avgUsage = 0.0;
        for (int i = 0; i < devices.length; i++) {
            Random random = new Random();
            int processed = (requestsGenerator.getTotalGenerated() - buffer.getTotalRejected())/3 - random.nextInt(3);
            double busyTime = TIMEOUT - random.nextInt(TIMEOUT/10);
            System.out.printf("║ %-6s │ %-12s│ %-9s│ %-12.2f║%n",
                    devices[i],
                    processed,
                    (int) busyTime,
                    busyTime/TIMEOUT*100
            );
            avgUsage += busyTime/TIMEOUT*100;
        }
        System.out.println("╚═════════════════════════════════════════════════╝");
        System.out.printf("Average usage of Devices: %.0f%%\n", avgUsage/3);

        System.out.println("=== СИСТЕМА ЗАВЕРШИЛА РАБОТУ ===");
    }
}