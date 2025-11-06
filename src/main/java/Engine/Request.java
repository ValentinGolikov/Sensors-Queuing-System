package Engine;

public class Request {
    private final String id;
    private final DateTime generationTime;
    private Priority priority;
    private RequestStatus status;
    private String data;

    public Request() {
        this.id = Hashing.hash();
        this.generationTime = new DateTime();
        this.status = RequestStatus.NEW;
    }

    public String getId() {
        return id;
    }
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    public Priority getPriority() {
        return priority;
    }
    public DateTime getGenerationTime() {
        return generationTime;
    }
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
    public RequestStatus getStatus() {
        return status;
    }
    public void setData(String data) {
        this.data = data;
    }
    public String getData() {
        return data;
    }

}
