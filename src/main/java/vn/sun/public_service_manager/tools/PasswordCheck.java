package vn.sun.public_service_manager.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordCheck {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java PasswordCheck <plain> <hash>");
            System.exit(1);
        }
        String plain = args[0];
        String hash = args[1];
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches(plain, hash);
        System.out.println(matches ? "MATCH" : "NO_MATCH");
    }
}
