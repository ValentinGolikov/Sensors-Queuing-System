package Engine.Threads;

import Engine.Priority;
import Engine.Request;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

// Поток для CRITICAL заявок (самый редкий)
public class CriticalGenerator implements Runnable {
    private final AtomicBoolean running;
    private final Random random = new Random();

    public CriticalGenerator (AtomicBoolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " запущен");

        while (running.get()) {
            try {
                // CRITICAL генерируются редко - раз в 2-3 секунды
                Thread.sleep(2000 + random.nextInt(1000));

                Request request = new Request(Priority.CRITICAL);

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
