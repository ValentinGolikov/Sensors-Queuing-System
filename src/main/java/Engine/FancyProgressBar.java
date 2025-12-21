package Engine;

import java.util.concurrent.atomic.AtomicBoolean;

public class FancyProgressBar {
    private static final String[] SPINNER = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
    private static Thread progressThread;
    private static AtomicBoolean running = new AtomicBoolean(false);
    private static int timeout;

    public static void start(int time) {
        if (running.get()) return;

        timeout = time - 100;
        running.set(true);

        // Поток для обновления прогресса
        progressThread = new Thread(() -> {
            int spinnerIndex = 0;
            long startTime = System.currentTimeMillis();

            while (running.get()) {
                long elapsed = System.currentTimeMillis() - startTime;
                int currentProgress = (int) Math.min(100, (elapsed * 100) / timeout);

                // Отрисовка прогресс-бара
                drawAnimatedProgressBar(currentProgress, SPINNER[spinnerIndex % SPINNER.length]);

                spinnerIndex++;
                try {
                    Thread.sleep(80); // Частота обновления
                } catch (InterruptedException e) {
                    break;
                }

                // Проверяем, не истекло ли время
                if (elapsed >= timeout) {
                    stop();
                    break;
                }
            }
        });

        progressThread.setDaemon(true);
        progressThread.start();
    }

    public static void stop() {
        running.set(false);
        if (progressThread != null) {
            progressThread.interrupt();
        }
        // Очищаем строку
        System.out.print("\r" + " ".repeat(80) + "\r");
    }

    private static void drawAnimatedProgressBar(int percentage, String spinner) {
        int barWidth = 30;
        int filled = (percentage * barWidth) / 100;
        int empty = barWidth - filled;

        String bar = "▰".repeat(filled) + "▱".repeat(empty);
        String progressText = String.format(" %s %3d%% %s", spinner, percentage, bar);

        System.out.print("\r" + progressText);
    }
}