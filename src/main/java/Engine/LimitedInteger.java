package Engine;

public class LimitedInteger {
    private final int maxValue;
    private int value;

    public LimitedInteger(int maxValue) {
        this(maxValue, 0);
    }

    public LimitedInteger(int maxValue, int initialValue) {
        if (maxValue <= 0) {
            throw new IllegalArgumentException("Max value must be > 0");
        }
        this.maxValue = maxValue;
        this.value = initialValue % (maxValue + 1);
    }

    public void increment() {
        value++;
        if (value > maxValue) {
            value = 0;
        }
    }

    public int getValue() {
        return value;
    }
}