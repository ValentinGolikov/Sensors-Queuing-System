package Engine.Threads;

import Engine.Controller;
import Engine.DataPack;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static Engine.TrainConfig.*;

public class MetricsGenerator implements Runnable {
    private final AtomicBoolean running;
    private final Controller controller;
    private final AtomicInteger totalGenerated;
    private final AtomicInteger metricsGenerated;

    public MetricsGenerator (AtomicBoolean running, Controller controller, AtomicInteger totalGenerated, AtomicInteger metricsGenerated) {
        this.running = running;
        this.controller = controller;
        this.totalGenerated = totalGenerated;
        this.metricsGenerated = metricsGenerated;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " запущен");

        while (running.get()) {
            try {
                ThreadPauser.checkPause();
                Thread.sleep(1000);

                controller.submitDataPack(DataPack.createNormalScenario(
                        DataPack.getRandomElement(TRAINS),
                        DataPack.getRandomElement(CARRIAGES),
                        DataPack.getRandomElement(WHEELS)
                ));

                totalGenerated.incrementAndGet();
                metricsGenerated.incrementAndGet();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println(Thread.currentThread().getName() + " завершен");
    }
}
