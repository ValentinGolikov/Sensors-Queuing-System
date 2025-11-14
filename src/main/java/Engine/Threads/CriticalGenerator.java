package Engine.Threads;

import Engine.Controller;
import Engine.DataPack;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static Engine.TrainConfig.*;

public class CriticalGenerator implements Runnable {
    private final AtomicBoolean running;
    private final Controller controller;
    private final AtomicInteger totalGenerated;

    public CriticalGenerator (AtomicBoolean running, Controller controller, AtomicInteger totalGenerated) {
        this.running = running;
        this.controller = controller;
        this.totalGenerated = totalGenerated;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " запущен");

        while (running.get()) {
            try {
                Thread.sleep(5000);

                controller.submitDataPack(DataPack.createCriticalScenario(
                        DataPack.getRandomElement(TRAINS),
                        DataPack.getRandomElement(CARRIAGES),
                        DataPack.getRandomElement(WHEELS)
                ));

                totalGenerated.incrementAndGet();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println(Thread.currentThread().getName() + " завершен");
    }
}
