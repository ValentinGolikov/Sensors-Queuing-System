package Engine.Threads;

import Engine.Controller;
import Engine.DataPack;

import java.util.concurrent.atomic.AtomicBoolean;

import static Engine.TrainConfig.*;

public class WarningGenerator implements Runnable {
    private final AtomicBoolean running;
    private final Controller controller;

    public WarningGenerator (AtomicBoolean running, Controller controller) {
        this.running = running;
        this.controller = controller;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " запущен");

        while (running.get()) {
            try {
                Thread.sleep(2500);

                controller.submitDataPack(DataPack.createWarningScenario(
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
