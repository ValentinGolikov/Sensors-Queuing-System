package Engine;

import Engine.Threads.RequestsGenerator;
import Engine.Threads.ThreadPauser;
import Engine.Tracking.ManualModeController;
import Engine.Tracking.RequestTracker;

import java.util.Scanner;

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
            // В АВТОМАТИЧЕСКОМ РЕЖИМЕ: сразу запускаем потоки
            controllerThread.start();
            receptionDispatcherThread.start();
            requestsGeneratorThread.start();
            selectionDispatcherThread.start();

            try {
                controllerThread.join();
                receptionDispatcherThread.join();
                requestsGeneratorThread.join();
                selectionDispatcherThread.join();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
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
            System.out.println("\n🚦 СИСТЕМА В РУЧНОМ РЕЖИМЕ");
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
                    System.out.println("Неизвестная команда. Доступные команды: [Enter], 's', 'start', 'q'");
                    break;
            }
        }

        // Останавливаем систему если она была запущена
        if (systemStarted) {
            stopAllComponents(requestsGenerator, selectionDispatcher, controller,
                    receptionDispatcher);
        }

        scanner.close();
    }

    private static void stopAllComponents(RequestsGenerator requestsGenerator, SelectionDispatcher selectionDispatcher,
                                          Controller controller, ReceptionDispatcher receptionDispatcher) {
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

        // Финальная статистика
        System.out.println("\n📊 ФИНАЛЬНАЯ СТАТИСТИКА:");
        System.out.println("Всего обработано заявок: " + RequestTracker.getTotalProcessed());
        System.out.println("=== СИСТЕМА ЗАВЕРШИЛА РАБОТУ ===");
    }
}