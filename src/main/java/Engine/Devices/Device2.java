package Engine.Devices;

import Engine.NotificationSystem;
import Engine.Request;
import Engine.Priority;

public class Device2 extends Engine.Devices.Device {

    public Device2() {
        super("Device2");
    }

    @Override
    protected void handleRequest(Request request) {
        // Формирование отчета-предупреждения
        System.out.println("Device2: Формирование отчета-ПРЕДУПРЕЖДЕНИЯ для заявки " + request.getId());

        // Отправка уведомления инженеру
        NotificationSystem.sendNotification(request, getName());

        // Сохранение в базу данных
        saveToDatabase(request);
    }

    @Override
    public boolean canHandle(Priority priority) {
        // Device2 обрабатывает только предупреждения
        return priority == Priority.WARNING;
    }

    private void saveToDatabase(Request request) {
        System.out.println("Device2: Сохранение отчета в БД для заявки " + request.getId());
    }
}