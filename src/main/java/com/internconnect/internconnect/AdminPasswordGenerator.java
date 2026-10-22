
package com.internconnect.internconnect;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Scanner;

public class AdminPasswordGenerator {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter admin password (minimum 8 characters): ");
        String password = scanner.nextLine();

        if (password.length() < 8 || password.length() > 72) {
            System.out.println("Password must be 8 to 72 characters.");
            scanner.close();
            return;
        }

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        String hashedPassword = encoder.encode(password);

        System.out.println("\nBCrypt password hash:");
        System.out.println(hashedPassword);

        scanner.close();
    }
}
