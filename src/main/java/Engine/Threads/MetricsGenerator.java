package Engine.Threads;

import Engine.Controller;
import Engine.DataPack;

import java.util.concurrent.atomic.AtomicBoolean;

import static Engine.TrainConfig.*;

public class MetricsGenerator implements Runnable {
    private final AtomicBoolean running;
    private final Controller controller;

    public MetricsGenerator (AtomicBoolean running, Controller controller) {
        this.running = running;
        this.controller = controller;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " запущен");

        while (running.get()) {
            try {
                Thread.sleep(300);

                controller.submitDataPack(DataPack.createNormalScenario(
                        DataPack.getRandomElement(TRAINS),
                        DataPack.getRandomElement(CARRIAGES),
                        DataPack.getRandomElement(WHEELS)
                ));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println(Thread.currentThread().getName() + " завершен");
    }
}
