package Engine;

import java.util.ArrayList;
import java.util.Arrays;

public class Buffer {
    private final int size;
    private int ptr;
    private boolean isFull;
    private final ArrayList<Request> requests;

    public Buffer(int size){
        this.size = size;
        this.ptr = 0;
        this.isFull = false;
        this.requests = new ArrayList<>(Arrays.asList(new Request[size]));
    }

    public int getSize() {
        return size;
    }

    public void addRequest(Request request) {
        requests.set(ptr, request);
        System.out.println(this.ptr);
        if (++this.ptr >= this.size){
            this.ptr = 0;
        }
    }

    public void getNextRequest() {

    }
}
