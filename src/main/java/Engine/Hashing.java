package Engine;

import java.util.Random;

public class Hashing {
    static final String dict = "abcdefghijklmnopqrstuvwxyz0123456789";
    static final Random rnd = new Random();
    public static String hash() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i <= 20; i++) {
            id.append(dict.charAt(rnd.nextInt(dict.length())));
        }
        return id.toString();
    }
}
