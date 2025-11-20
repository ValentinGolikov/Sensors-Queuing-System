package Engine;

import java.time.Duration;
import java.time.Instant;

public class PauseTimeManager {
    private static Instant pauseStartTime;
    private static Instant pauseEndTime;
    private static boolean isPaused = false;

    public static void setPauseStartTime() {
        pauseStartTime = Instant.now();
        isPaused = true;
    }

    public static void setPauseEndTime() {
        if (pauseStartTime != null && isPaused) {
            pauseEndTime = Instant.now();
            isPaused = false;
        }
    }

    public static long getPauseDuration() {
        return Duration.between(pauseStartTime, pauseEndTime).toMillis();
    }
}
