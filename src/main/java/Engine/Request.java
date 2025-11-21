package Engine;

import Engine.Tracking.RequestTracker;

import java.time.Duration;

public class Request {
    private static int nextId = 1;
    private final int id;
    private final DateTime generationTime;
    private final Priority priority;
    private RequestStatus status;
    private String data;

    public Request(Priority priority) {
        this.id = nextId++;
        this.generationTime = new DateTime();
        this.priority = priority;
        this.status = RequestStatus.NEW;
        RequestTracker.trackCreated(this);
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
    public void setData(String data) {
        this.data = data;
    }
    public int getId() {
        return id;
    }
    public Priority getPriority() {
        return priority;
    }
    public DateTime getGenerationTime() {
        return generationTime;
    }
    public String getData() {
        return data;
    }
    public RequestStatus getStatus() {
        return status;
    }

}
