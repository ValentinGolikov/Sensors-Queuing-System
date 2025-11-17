package Engine;

import javax.swing.*;
import java.util.ArrayList;

public class Buffer {
    private final int size;
    private final LimitedInteger ptr;
    private final ArrayList<Request> requests;

    private boolean hasSpace(){
        for (Request request : requests) {
            if (request == null) {
                return true;
            }
        }
        return false;
    }

    public Buffer(int size){
        this.size = size;
        this.ptr = new LimitedInteger(size - 1);
        this.requests = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            requests.add(null);
        }
    }

    public int getSize() {
        return size;
    }

    public RequestStatus addRequest(Request request) {
        if (!hasSpace()) {
            System.out.println("===================INIT REJECTION=================== " + requests.get(ptr.getValue()).getPriority() + " " + requests.get(ptr.getValue()).getId());
        }
        request.setStatus(RequestStatus.IN_BUFFER);
        requests.set(ptr.getValue(), request);
        this.ptr.increment();
        return RequestStatus.IN_BUFFER;
    }

    public Request getNextRequest() {
        LimitedInteger pointer = new LimitedInteger(this.size - 1, this.ptr.getValue());
        while (requests.get(pointer.getValue()) == null) {
            pointer.increment();
        }
        Request req = requests.get(pointer.getValue());
        Priority priority = req.getPriority();

        int current_pointer = pointer.getValue();
        pointer.increment();
        Request tempReq;

        while(pointer.getValue() != this.ptr.getValue()) {
            tempReq = requests.get(pointer.getValue());
            if (tempReq != null) {
                if (tempReq.getPriority().ordinal() < priority.ordinal()) {
                    priority = tempReq.getPriority();
                    req = tempReq;
                    current_pointer = pointer.getValue();
                }
                else if (tempReq.getPriority().ordinal() == priority.ordinal()) {
                    if (tempReq.getGenerationTime().compareTo(req.getGenerationTime()) < 0) {
                        current_pointer = pointer.getValue();
                        req = tempReq;
                    }
                }
            }
        pointer.increment();
        }
        requests.set(current_pointer, null);
        this.ptr.increment();
        req.setStatus(RequestStatus.PROCESSED);
        return req;
    }

    public void removeRequest() {
        requests.set(this.ptr.getValue(), null);
        this.ptr.increment();
    }
}
