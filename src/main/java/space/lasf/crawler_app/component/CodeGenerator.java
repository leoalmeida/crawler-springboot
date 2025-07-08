package space.lasf.crawler_app.component;

import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

@Component
public class CodeGenerator {

    private static final int CODE_LENGTH = 8;
    private static final String SALT = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public String generateRandomCode() {
        StringBuilder codeBuilder = new StringBuilder();
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            for (int i = 0; i < CODE_LENGTH; i++) {
                int randomIndex = random.nextInt(SALT.length());
                codeBuilder.append(SALT.charAt(randomIndex));
            }
        } catch (NoSuchAlgorithmException e) {
            Random random = new Random();
            for (int i = 0; i < CODE_LENGTH; i++) {
                int randomIndex = random.nextInt(SALT.length());
                codeBuilder.append(SALT.charAt(randomIndex));
            }
        }
        return codeBuilder.toString();
    }
}