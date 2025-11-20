package Engine.Threads;

import Engine.Controller;
import Engine.DataPack;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static Engine.TrainConfig.*;

public class WarningGenerator implements Runnable {
    private final AtomicBoolean running;
    private final Controller controller;
    private final AtomicInteger totalGenerated;
    private final AtomicInteger warningGenerated;

    public WarningGenerator (AtomicBoolean running, Controller controller, AtomicInteger totalGenerated, AtomicInteger warningGenerated) {
        this.running = running;
        this.controller = controller;
        this.totalGenerated = totalGenerated;
        this.warningGenerated = warningGenerated;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " запущен");

        while (running.get()) {
            try {
                ThreadPauser.checkPause();
                Thread.sleep(5000);

                controller.submitDataPack(DataPack.createWarningScenario(
                        DataPack.getRandomElement(TRAINS),
                        DataPack.getRandomElement(CARRIAGES),
                        DataPack.getRandomElement(WHEELS)
                ));

                totalGenerated.incrementAndGet();
                warningGenerated.incrementAndGet();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println(Thread.currentThread().getName() + " завершен");
    }
}
