
package com.internconnect.internconnect.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final JdbcTemplate jdbcTemplate;

    public AdminDashboardController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(HttpSession session) {

        // Only a logged-in admin can view dashboard statistics.
        if (session.getAttribute("adminId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Please log in as an admin."
                    ));
        }

        Integer internships = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM internships",
                Integer.class
        );

        Integer students = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM students",
                Integer.class
        );

        Integer applications = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM applications",
                Integer.class
        );

        return ResponseEntity.ok(
                Map.of(
                        "totalInternships",
                        internships == null ? 0 : internships,
                        "totalStudents",
                        students == null ? 0 : students,
                        "totalApplications",
                        applications == null ? 0 : applications
                )
        );
    }
}
