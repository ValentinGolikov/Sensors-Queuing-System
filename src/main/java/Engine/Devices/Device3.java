package Engine.Devices;

import Engine.Request;
import Engine.Priority;

public class Device3 extends Engine.Devices.Device {

    public Device3() {
        super("Device3");
    }

    @Override
    protected void handleRequest(Request request) {
        // Обработка метрик
        System.out.println("Device3: Обработка МЕТРИК для заявки " + request.getId());

        // Сохранение данных метрик в базу данных
        saveMetricsToDatabase(request);
    }

    @Override
    public boolean canHandle(Priority priority) {
        // Device3 обрабатывает только метрики
        return priority == Priority.METRICS;
    }

    private void saveMetricsToDatabase(Request request) {
        System.out.println("Device3: Сохранение данных метрик в БД для заявки " + request.getId());
    }
}