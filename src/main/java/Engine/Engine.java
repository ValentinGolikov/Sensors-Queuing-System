package Engine;

import Engine.Threads.RequestsGenerator;

public class Engine {
    static void main(String[] args) {

        try {
            RequestsGenerator.start();
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

        Buffer buf = new Buffer(9);
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
