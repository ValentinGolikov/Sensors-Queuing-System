package Engine;

import java.util.List;
import java.util.Random;

public record DataPack(String trainNumber, String trainCarriageNumber, String wheelNumber, String metrics) {
    /**
     * КРИТИЧЕСКАЯ ситуация - требует немедленного вмешательства
     */
    public static DataPack createCriticalScenario(String trainNum, String carriageNum, String wheelNum) {
        Random rand = new Random();
        // Критические показатели: температура выше 120°C, давление ниже 1.5 bar, износ более 90%
        double temperature = 120 + rand.nextDouble() * 30; // 120-150°C (перегрев)
        double pressure = 1.0 + rand.nextDouble() * 0.5;   // 1.0-1.5 bar (опасно низкое)
        double wear = 90 + rand.nextDouble() * 10;         // 90-100% (почти полный износ)
        double vibration = 8 + rand.nextDouble() * 4;      // 8-12 mm/s (опасная вибрация)

        String metrics = String.format(
                "\ttemp:%.1fC,\n\tpressure:%.1fbar,\n\twear:%.1f%%,\n\tvibration:%.1fmm/s,\n\tstatus:CRITICAL",
                temperature, pressure, wear, vibration
        );

        return new DataPack(trainNum, carriageNum, wheelNum, metrics);
    }

    /**
     * ПРЕДУПРЕЖДАЮЩАЯ ситуация - требует внимания и мониторинга
     */
    public static DataPack createWarningScenario(String trainNum, String carriageNum, String wheelNum) {
        Random rand = new Random();
        // Предупреждающие показатели: температура 90-120°C, давление 1.5-2.0 bar, износ 70-90%
        double temperature = 90 + rand.nextDouble() * 30;  // 90-120°C (повышенная)
        double pressure = 1.5 + rand.nextDouble() * 0.5;   // 1.5-2.0 bar (пониженное)
        double wear = 70 + rand.nextDouble() * 20;         // 70-90% (повышенный износ)
        double vibration = 4 + rand.nextDouble() * 4;      // 4-8 mm/s (умеренная вибрация)

        String metrics = String.format(
                "\ttemp:%.1fC,\n\tpressure:%.1fbar,\n\twear:%.1f%%,\n\tvibration:%.1fmm/s,\n\tstatus:WARNING",
                temperature, pressure, wear, vibration
        );

        return new DataPack(trainNum, carriageNum, wheelNum, metrics);
    }

    /**
     * НОРМАЛЬНАЯ ситуация - метрики в пределах нормы
     */
    public static DataPack createNormalScenario(String trainNum, String carriageNum, String wheelNum) {
        Random rand = new Random();
        // Нормальные показатели: температура 60-90°C, давление 2.0-3.0 bar, износ 0-70%
        double temperature = 60 + rand.nextDouble() * 30;  // 60-90°C (рабочая)
        double pressure = 2.0 + rand.nextDouble();   // 2.0-3.0 bar (нормальное)
        double wear = rand.nextDouble() * 70;              // 0-70% (допустимый износ)
        double vibration = 1 + rand.nextDouble() * 3;      // 1-4 mm/s (нормальная вибрация)

        String metrics = String.format(
                "\ttemp:%.1fC,\n\tpressure:%.1fbar,\n\twear:%.1f%%,\n\tvibration:%.1fmm/s,\n\tstatus:NORMAL",
                temperature, pressure, wear, vibration
        );

        return new DataPack(trainNum, carriageNum, wheelNum, metrics);
    }

    public static <T> T getRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Random random = new Random();
        int randomIndex = random.nextInt(list.size());
        return list.get(randomIndex);
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public String getTrainCarriageNumber() {
        return trainCarriageNumber;
    }

    public String getWheelNumber() {
        return wheelNumber;
    }

    public String getMetrics() {
        return metrics;
    }
}
