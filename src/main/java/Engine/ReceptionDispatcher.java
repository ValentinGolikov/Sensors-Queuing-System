package Engine;

import Engine.Threads.RequestsGenerator;

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
                    Request request = controller.getRequestsQueue().take();

                    RequestStatus requestStatus = buffer.addRequest(request);
                    if (requestStatus.equals(RequestStatus.REJECTED)) {
                        System.out.println("Request with ID " + request.getId() + " REJECTED");
                    } else {
                        System.out.println("Request with ID " + request.getId() + " IN_BUFFER");
                    }

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
