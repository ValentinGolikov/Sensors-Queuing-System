package Engine;

import Engine.Threads.RequestsGenerator;
import Engine.Threads.ThreadPauser;

public class ReceptionDispatcher implements Runnable {
    private final Controller controller;
    private final Buffer buffer;
    private boolean isRunning = true;

    public ReceptionDispatcher(Controller controller, Buffer buffer) {
        this.controller = controller;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        System.out.println("ReceptionDispatcher started");

        while (isRunning) {
            if (controller.getRequestsQueue().isEmpty()) {
                if (RequestsGenerator.isStopped()) {
                    isRunning = false;
                }
            }
            else {
                try {
                    ThreadPauser.checkPause();
                    Request request = controller.getRequestsQueue().take();


                    System.out.println("Request with ID " + request.getId() + " " + buffer.addRequest(request));

                } catch (InterruptedException e) {
                    System.out.println("ReceptionDispatcher interrupted");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in ReceptionDispatcher: " + e.getMessage());
                }
            }
        }

        System.out.println("ReceptionDispatcher stopped");
    }
}
