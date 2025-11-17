package Engine;

import Engine.Threads.RequestsGenerator;

public class Engine {
    public static void main(String[] args) {
        Buffer buf = new Buffer(10);
        Controller controller = new Controller();

        RequestsGenerator requestsGenerator = new RequestsGenerator(controller);
        ReceptionDispatcher receptionDispatcher = new ReceptionDispatcher(controller, buf);
        SelectionDispatcher selectionDispatcher = new SelectionDispatcher(buf);

        Thread receptionDispatcherThread = new Thread(receptionDispatcher, "receptionDispatcherThread");
        Thread controllerThread = new Thread(controller, "controllerThread");
        Thread requestsGeneratorThread = new Thread(requestsGenerator, "requestsGeneratorThread");
        Thread selectionDispatcherThread = new Thread(selectionDispatcher, "selectionDispatcherThread");

        controllerThread.start();
        receptionDispatcherThread.start();
        requestsGeneratorThread.start();
        selectionDispatcherThread.start();

        try {
            controllerThread.join();
            receptionDispatcherThread.join();
            requestsGeneratorThread.join();
            selectionDispatcherThread.join();
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
}
