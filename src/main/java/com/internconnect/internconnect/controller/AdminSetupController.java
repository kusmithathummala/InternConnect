
package com.internconnect.internconnect.controller;

import com.internconnect.internconnect.model.Admin;
import com.internconnect.internconnect.repository.AdminRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/setup")
public class AdminSetupController {

    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    public AdminSetupController(
            JdbcTemplate jdbcTemplate,
            AdminRepository adminRepository) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping
    public ResponseEntity<?> createFirstAdmin(
            @RequestBody Admin admin) {

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admins",
                Integer.class);

        if (count != null && count > 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message",
                            "An admin account already exists."));
        }

        if (admin.getFullName() == null
                || admin.getFullName().isBlank()
                || admin.getEmail() == null
                || admin.getEmail().isBlank()
                || admin.getPassword() == null
                || admin.getPassword().length() < 8
                || admin.getPassword().length() > 72) {

            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            "Enter a name, valid email, and password of 8–72 characters."));
        }

        String email = admin.getEmail().trim().toLowerCase();

        String hash = encoder.encode(admin.getPassword());

        jdbcTemplate.update(
                """
                INSERT INTO admins (full_name, email, password_hash)
                VALUES (?, ?, ?)
                """,
                admin.getFullName().trim(),
                email,
                hash
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message",
                        "Admin account created successfully.")
        );
    }
}
