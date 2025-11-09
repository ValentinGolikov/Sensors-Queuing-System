package Engine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTime {
    private final LocalDateTime dateTime;

    public DateTime(){
        this.dateTime = LocalDateTime.now();
    }

    public String format() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }

    public static DateTime now() {
        return new DateTime();
    }

    public int compareTo(DateTime other) {
        return this.dateTime.compareTo(other.dateTime);
    }

}