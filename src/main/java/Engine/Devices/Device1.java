package Engine.Devices;

import Engine.NotificationSystem;
import Engine.Request;
import Engine.Priority;
import Engine.Threads.ThreadPauser;

public class Device1 extends Engine.Devices.Device {

    public Device1() {
        super("Device1");
    }

    @Override
    protected void handleRequest(Request request) {
        try {
            ThreadPauser.checkPause();
            // Формирование критического отчета или отчета-предупреждения
            if (request.getPriority() == Priority.CRITICAL) {
                System.out.println("Device1: Формирование КРИТИЧЕСКОГО отчета для заявки " + request.getId());
                NotificationSystem.sendNotification(request, getName());
            } else {
                System.out.println("Device1: Формирование отчета-ПРЕДУПРЕЖДЕНИЯ для заявки " + request.getId());
                NotificationSystem.sendNotification(request, getName());
            }
            // Сохранение в базу данных
            saveToDatabase(request);

            System.out.println("Device1: sleeping for " + (long) Math.exp((double) getProcessedCount()/10));
            Thread.sleep((long) Math.exp((double) getProcessedCount()/10));
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }

    @Override
    public boolean canHandle(Priority priority) {
        // Device1 обрабатывает критические заявки и предупреждения (когда Device2 занят)
        return priority == Priority.CRITICAL || priority == Priority.WARNING;
    }

    private void saveToDatabase(Request request) {
        System.out.println("Device1: Сохранение отчета в БД для заявки " + request.getId());
        // Логика сохранения в базу данных
    }
}