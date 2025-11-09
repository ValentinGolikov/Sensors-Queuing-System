package Engine.Threads;

import Engine.Priority;
import Engine.Request;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

// Поток для METRICS заявок (самая высокая нагрузка)
public class MetricsGenerator implements Runnable {
    private final AtomicBoolean running;
    private final Random random = new Random();

    public MetricsGenerator (AtomicBoolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " запущен");

        while (running.get()) {
            try {
                // METRICS генерируются очень часто - 3-5 заявок в секунду
                Thread.sleep(200 + random.nextInt(300));

                Request request = new Request(Priority.METRICS);

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
