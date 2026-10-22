
package com.internconnect.internconnect.controller;

import com.internconnect.internconnect.model.Admin;
import com.internconnect.internconnect.repository.AdminRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminRepository adminRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AdminAuthController(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    // Admin Login
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Admin admin,
            HttpSession session) {

        if (admin.getEmail() == null
                || admin.getPassword() == null
                || admin.getEmail().isBlank()
                || admin.getPassword().isEmpty()) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Email and password are required."
                    )
            );
        }

        String email = admin.getEmail()
                .trim()
                .toLowerCase();

        Admin existingAdmin =
                adminRepository.findByEmail(email);

        if (existingAdmin == null
                || existingAdmin.getPassword() == null
                || !passwordEncoder.matches(
                        admin.getPassword(),
                        existingAdmin.getPassword())) {

            return ResponseEntity.status(
                    HttpStatus.UNAUTHORIZED
            ).body(
                    Map.of(
                            "message",
                            "Invalid admin email or password."
                    )
            );
        }

        // Store the admin ID in the session.
        session.setAttribute(
                "adminId",
                existingAdmin.getId()
        );

        // Remove the student login identity.
        session.removeAttribute("studentId");

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Admin login successful.",
                        "id",
                        existingAdmin.getId(),
                        "fullName",
                        existingAdmin.getFullName(),
                        "email",
                        existingAdmin.getEmail()
                )
        );
    }

    // Admin Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Admin logged out successfully."
                )
        );
    }
}
