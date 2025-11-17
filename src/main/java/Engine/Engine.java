package Engine;

import Engine.Threads.RequestsGenerator;
<<<<<<< HEAD
=======
import Engine.Threads.ThreadPauser;
import Engine.Tracking.ManualModeController;
import Engine.Tracking.RequestTracker;

import java.util.Scanner;
>>>>>>> 148240b (auto_mode_v0.3.1)

public class Engine {
    public static void main(String[] args) {
        Buffer buf = new Buffer(10);
        Controller controller = new Controller();

        RequestsGenerator requestsGenerator = new RequestsGenerator(controller);
        ReceptionDispatcher receptionDispatcher = new ReceptionDispatcher(controller, buf);
        SelectionDispatcher selectionDispatcher = new SelectionDispatcher(buf);

        Thread receptionDispatcherThread = new Thread(receptionDispatcher, "receptionDispatcherThread");
        Thread controllerThread = new Thread(controller, "controllerThread");
        Thread requestsGeneratorThread = new Thread(requestsGenerator, "requestsGeneratorThread");
        Thread selectionDispatcherThread = new Thread(selectionDispatcher, "selectionDispatcherThread");

<<<<<<< HEAD
        controllerThread.start();
        receptionDispatcherThread.start();
        requestsGeneratorThread.start();
        selectionDispatcherThread.start();
=======
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
                case "s":
                case "S":
                    if (systemStarted) {
                        ThreadPauser.resumeAllThreads();
                        Thread.sleep(100);
                        ThreadPauser.pauseAllThreads();
                        manualController.displaySystemState(buffer, selectionDispatcher);
                        System.out.print("\nВведите Enter чтобы продолжить: ");
                        input = scanner.nextLine().trim();
                    } else {
                        System.out.println("❌ Сначала запустите систему командой 'start'");
                    }
                    break;

                case "start":
                    if (!systemStarted) {
                        System.out.println("🚀 ЗАПУСК ВСЕХ ПОТОКОВ...");
                        controllerThread.start();
                        receptionDispatcherThread.start();
                        requestsGeneratorThread.start();
                        selectionDispatcherThread.start();
                        systemStarted = true;

                        ThreadPauser.pauseAllThreads();
                        manualController.displaySystemState(buffer, selectionDispatcher);
                    } else {
                        System.out.println("✅ Система уже запущена");
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

    private static void runAutomaticForTime(long milliseconds, Buffer buffer, SelectionDispatcher selectionDispatcher) {
        System.out.println("\n⏱️  АВТОМАТИЧЕСКИЙ РЕЖИМ на " + (milliseconds / 1000) + " секунд...");
        manualController.setPaused(false);
>>>>>>> 148240b (auto_mode_v0.3.1)

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
