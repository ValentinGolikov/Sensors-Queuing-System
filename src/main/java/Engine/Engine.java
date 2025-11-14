package Engine;

import Engine.Threads.RequestsGenerator;

public class Engine {
    static void main(String[] args) {
        Buffer buf = new Buffer(30);
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

        Request req1 = new Request(Priority.METRICS);
        Request req2 = new Request(Priority.CRITICAL);
        Request req3 = new Request(Priority.WARNING);
        Request req4 = new Request(Priority.WARNING);
        Request req5 = new Request(Priority.CRITICAL);
        Request req6 = new Request(Priority.WARNING);
        Request req7 = new Request(Priority.METRICS);
        Request req8 = new Request(Priority.METRICS);
        Request req9 = new Request(Priority.WARNING);

        buf.addRequest(req1);
        buf.addRequest(req2);
        buf.addRequest(req3);
        buf.addRequest(req4);
        buf.addRequest(req5);
        buf.addRequest(req6);
        buf.addRequest(req7);
        buf.addRequest(req8);
        buf.addRequest(req9);

        Request result = buf.getNextRequest();
        System.out.println(result.getId() + " " + result.getPriority() +  " " + result.getGenerationTime().format());

        result = buf.getNextRequest();
        System.out.println(result.getId() + " " + result.getPriority() +  " " + result.getGenerationTime().format());

        result = buf.getNextRequest();
        System.out.println(result.getId() + " " + result.getPriority() +  " " + result.getGenerationTime().format());

        result = buf.getNextRequest();
        System.out.println(result.getId() + " " + result.getPriority() +  " " + result.getGenerationTime().format());

        result = buf.getNextRequest();
        System.out.println(result.getId() + " " + result.getPriority() +  " " + result.getGenerationTime().format());

        result = buf.getNextRequest();
        System.out.println(result.getId() + " " + result.getPriority() +  " " + result.getGenerationTime().format());

        result = buf.getNextRequest();
        System.out.println(result.getId() + " " + result.getPriority() +  " " + result.getGenerationTime().format());

        result = buf.getNextRequest();
        System.out.println(result.getId() + " " + result.getPriority() +  " " + result.getGenerationTime().format());

        result = buf.getNextRequest();
        System.out.println(result.getId() + " " + result.getPriority() +  " " + result.getGenerationTime().format());
    }
}
