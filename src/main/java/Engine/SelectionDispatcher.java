package Engine;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SelectionDispatcher implements Runnable {
    private final Buffer buffer;
    private final BlockingQueue<Request> requestsQueue = new LinkedBlockingQueue<>();
    private static boolean isRunning = true;

    public SelectionDispatcher(Buffer buf){
        this.buffer = buf;
    }

    @Override
    public void run() {
        System.out.println("SelectionDispatcher started");

        while (isRunning){
            System.out.println(buffer.getNextRequest().getId()+ " " + " " + buffer.getNextRequest().getStatus());
        }
    }
}
