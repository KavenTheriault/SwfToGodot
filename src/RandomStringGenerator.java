import java.util.Random;

public class RandomStringGenerator {
    private final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    public String randomString(int length) {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = rand.nextInt(CHARS.length());
            char randomChar = CHARS.charAt(index);
            sb.append(randomChar);
        }
        return sb.toString();
    }
}
