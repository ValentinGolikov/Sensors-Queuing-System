package Engine.Devices;

import Engine.Request;
import Engine.Priority;
import Engine.Threads.ThreadPauser;

public class Device3 extends Engine.Devices.Device {

    public Device3() {
        super("Device3");
    }

    @Override
    protected void handleRequest(Request request) {
        try {
            ThreadPauser.checkPause();
            // Обработка метрик
            System.out.println("Device3: Обработка МЕТРИК для заявки " + request.getId());

            // Сохранение данных метрик в базу данных
            saveMetricsToDatabase(request);
            System.out.println("Device3: sleeping for " + (long) Math.exp((double) getProcessedCount() /10));
            Thread.sleep((long) Math.exp((double) getProcessedCount() /10));
        } catch (InterruptedException e) {
            System.err.println(e);
        }
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