package Engine.Threads;

import Engine.Priority;
import Engine.Request;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

// Поток для WARNING заявок (средняя частота)
public class WarningGenerator implements Runnable {
    private final AtomicBoolean running;
    private final Random random = new Random();

    public WarningGenerator (AtomicBoolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " запущен");

        while (running.get()) {
            try {
                // WARNING генерируются чаще - раз в 1-2 секунды
                Thread.sleep(1000 + random.nextInt(1000));

                Request request = new Request(Priority.WARNING);

                System.out.println(Thread.currentThread().getName() + " создал: " +
                        request.getPriority() + " (ID: " + request.getId() + ")");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println(Thread.currentThread().getName() + " завершен");
    }
}
