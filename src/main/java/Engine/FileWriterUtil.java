package Engine;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileWriterUtil {

    // Директория для сохранения файлов
    private static final String OUTPUT_DIR = "requests_data";

    static {
        // Создаем директорию при первом использовании
        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR));
        } catch (IOException e) {
            System.err.println("Не удалось создать директорию: " + OUTPUT_DIR);
        }
    }

    public static void saveRequestToFile(Request request) {
        String fileName = String.format("%s_%s_%s.txt",
                request.getId(),
                request.getGenerationTime().format(),
                request.getPriority()
        );

        // Заменяем недопустимые символы в имени файла
        fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");

        Path filePath = Paths.get(OUTPUT_DIR, fileName);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write(request.getData());
            System.out.println("Файл сохранен: " + filePath);
        } catch (IOException e) {
            System.err.println("Ошибка при записи файла " + fileName + ": " + e.getMessage());
        }
    }
}
