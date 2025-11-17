package Engine;

import static Engine.FileWriterUtil.saveRequestToFile;

public class NotificationSystem {
    public static void sendNotification(Request request, String deviceId) {
        System.out.println(deviceId + ": request " + request.getId() + " has " +
                request.getPriority() + ", report with data will be generated...");
        saveRequestToFile(request);
    }
}
