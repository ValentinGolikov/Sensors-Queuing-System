package Engine;

import Engine.Threads.RequestsGenerator;
import Engine.Tracking.RequestTracker;

import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Buffer {
    private final int size;
    private final AtomicInteger criticalRejected = new AtomicInteger(0);
    private final AtomicInteger warningRejected = new AtomicInteger(0);
    private final AtomicInteger metricsRejected = new AtomicInteger(0);
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

    public int getCurrentSize() {
        int count = 0;
        for (Request request: requests){
            if (request != null){
                count++;
            }
        }
        return count;
    }

    public int getCriticalRejected() {
        return criticalRejected.get();
    }

    public int getWarningRejected() {
        return warningRejected.get();
    }

    public int getMetricsRejected() {
        return metricsRejected.get();
    }

    public LimitedInteger getPtr() {
        return ptr;
    }

    public RequestStatus addRequest(Request request) {
        if (!hasSpace()) {
            System.out.println("===================INIT REJECTION=================== " +
                    requests.get(ptr.getValue()).getPriority() + " " + requests.get(ptr.getValue()).getId());
            requests.get(ptr.getValue()).setStatus(RequestStatus.REJECTED);

            Priority priority = requests.get(ptr.getValue()).getPriority();
            switch (priority) {
                case Priority.CRITICAL -> criticalRejected.incrementAndGet();
                case Priority.WARNING -> warningRejected.incrementAndGet();
                case Priority.METRICS -> metricsRejected.incrementAndGet();
            }

            RequestTracker.trackInBuffer(request);
            request.setStatus(RequestStatus.IN_BUFFER);
            requests.set(ptr.getValue(), request);
            this.ptr.increment();
        }
        else {
            if (requests.get(ptr.getValue()) == null) {
                RequestTracker.trackInBuffer(request);
                request.setStatus(RequestStatus.IN_BUFFER);
                requests.set(ptr.getValue(), request);
                this.ptr.increment();
            } else {
                this.ptr.increment();
            }
        }
        return RequestStatus.IN_BUFFER;

    }

    public Request getNextRequest(AtomicBoolean running) {
        int beginPoint = this.ptr.getValue();
        LimitedInteger pointer = new LimitedInteger(this.size - 1, beginPoint);
        while (running.get() && (requests.get(pointer.getValue()) == null)) {
            pointer.increment();
        }
        if (!running.get()) { return null; }
        Request req = requests.get(pointer.getValue());
        Priority priority = req.getPriority();

        int current_pointer = pointer.getValue();
        pointer.increment();
        Request tempReq;

        while(pointer.getValue() != beginPoint) {
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
        req.setStatus(RequestStatus.PROCESSED);
        return req;
    }

    public Request getRequest(int i) {
        return requests.get(i);
    }
}
