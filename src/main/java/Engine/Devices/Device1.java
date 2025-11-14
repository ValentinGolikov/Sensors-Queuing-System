package Engine.Devices;

import Engine.NotificationSystem;
import Engine.Request;
import Engine.Priority;

public class Device1 extends Engine.Devices.Device {

    public Device1() {
        super("Device1");
    }

    @Override
    protected void handleRequest(Request request) {
        // Формирование критического отчета или отчета-предупреждения
        if (request.getPriority() == Priority.CRITICAL) {
            System.out.println("Device1: Формирование КРИТИЧЕСКОГО отчета для заявки " + request.getId());
            // Отправка уведомления инженеру
            NotificationSystem.sendNotification(request, getName());
        } else {
            System.out.println("Device1: Формирование отчета-ПРЕДУПРЕЖДЕНИЯ для заявки " + request.getId());
            NotificationSystem.sendNotification(request, getName());
        }

        // Сохранение в базу данных
        saveToDatabase(request);
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