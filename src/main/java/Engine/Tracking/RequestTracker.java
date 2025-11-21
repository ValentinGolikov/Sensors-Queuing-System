package Engine.Tracking;

import Engine.Request;
import Engine.Priority;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestTracker {
    private static final ConcurrentHashMap<Integer, RequestInfo> activeRequests = new ConcurrentHashMap<>();
    private static final AtomicInteger totalProcessed = new AtomicInteger(0);

    public static class RequestInfo {
        private final int id;
        private final Priority priority;
        private final LocalDateTime creationTime;
        private LocalDateTime processedTime;
        private long lifeTime;
        private String status; // "CREATED", "IN_BUFFER", "IN_DEVICE", "PROCESSED"
        private String currentDevice;

        public RequestInfo(Request request) {
            this.id = request.getId();
            this.priority = request.getPriority();
            this.creationTime = LocalDateTime.now();
            this.status = "CREATED";
        }

        public void setLifeTime(long lifeTime) {
            this.lifeTime = lifeTime;
        }
        public long getLifeTime() {
            return this.lifeTime;
        }

        // Getters and setters
        public void setStatus(String status) { this.status = status; }
        public void setProcessedTime(LocalDateTime time) { this.processedTime = time; }
        public void setCurrentDevice(String device) { this.currentDevice = device; }

        public int getId() { return id; }
        public Priority getPriority() { return priority; }
        public String getStatus() { return status; }
        public String getCurrentDevice() { return currentDevice; }
        public LocalDateTime getCreationTime() { return creationTime; }
        public LocalDateTime getProcessedTime() {
            return processedTime;
        }
    }

    public static void trackCreated(Request request) {
        activeRequests.put(request.getId(), new RequestInfo(request));
    }

    public static void trackInBuffer(Request request) {
        RequestInfo info = activeRequests.get(request.getId());
        if (info != null) {
            info.setStatus("IN_BUFFER");
        }
    }

    public static void trackInDevice(Request request, String deviceName) {
        RequestInfo info = activeRequests.get(request.getId());
        if (info != null) {
            info.setStatus("IN_DEVICE");
            info.setCurrentDevice(deviceName);
        }
    }

    public static void trackProcessed(Request request) {
        RequestInfo info = activeRequests.get(request.getId());
        if (info != null) {
            info.setStatus("PROCESSED");
            info.setProcessedTime(LocalDateTime.now());
            totalProcessed.incrementAndGet();
        }
    }

    public static ConcurrentHashMap<Integer, RequestInfo> getActiveRequests() {
        return activeRequests;
    }

    public static int getTotalProcessed() {
        return totalProcessed.get();
    }
}
