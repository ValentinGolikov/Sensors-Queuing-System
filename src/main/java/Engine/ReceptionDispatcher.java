package Engine;

public class ReceptionDispatcher implements Runnable {
    private final Controller controller;
    private final Buffer buffer;
    private volatile boolean running = true;

    public ReceptionDispatcher(Controller controller, Buffer buffer) {
        this.controller = controller;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        System.out.println("ReceptionDispatcher started");

        while (running) {
            try {
                Request request = controller.getRequestsQueue().take();

                RequestStatus requestStatus = buffer.addRequest(request);
                if (requestStatus.equals(RequestStatus.REJECTED)) {
                    //create negative log, start rejecting handler
                }
                else {
                    //create positive log
                }


            } catch (InterruptedException e) {
                System.out.println("ReceptionDispatcher interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error in ReceptionDispatcher: " + e.getMessage());
            }
        }

        System.out.println("ReceptionDispatcher stopped");
    }

    public void stop() {
        running = false;
        Thread.currentThread().interrupt();
    }
}
