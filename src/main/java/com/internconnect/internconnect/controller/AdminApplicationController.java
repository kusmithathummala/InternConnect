
package com.internconnect.internconnect.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/applications")
public class AdminApplicationController {

    private final JdbcTemplate jdbcTemplate;

    public AdminApplicationController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private boolean isAdmin(HttpSession session) {
        return session.getAttribute("adminId") != null;
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("message", "Please log in as an administrator.")
        );
    }

    // View all student applications.
    @GetMapping
    public ResponseEntity<?> getAllApplications(HttpSession session) {

        if (!isAdmin(session)) {
            return unauthorized();
        }

        String sql = """
                SELECT
                    a.id AS applicationId,
                    a.student_id AS studentId,
                    s.full_name AS studentName,
                    s.email AS studentEmail,
                    a.internship_id AS internshipId,
                    i.title AS internshipTitle,
                    i.company AS company,
                    a.status AS status,
                    a.applied_at AS appliedAt
                FROM applications a
                INNER JOIN students s ON a.student_id = s.id
                INNER JOIN internships i ON a.internship_id = i.id
                ORDER BY a.applied_at DESC
                """;

        List<Map<String, Object>> applications =
                jdbcTemplate.queryForList(sql);

        return ResponseEntity.ok(applications);
    }

    // Update an application's status.
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable int id,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        if (!isAdmin(session)) {
            return unauthorized();
        }

        if (id <= 0) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Invalid application ID.")
            );
        }

        String status = request.get("status");

        if (status == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Application status is required.")
            );
        }

        status = status.trim();

        if (!status.equals("Pending")
                && !status.equals("Accepted")
                && !status.equals("Rejected")) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Status must be Pending, Accepted, or Rejected."
                    )
            );
        }

        int updated = jdbcTemplate.update(
                "UPDATE applications SET status = ? WHERE id = ?",
                status,
                id
        );

        if (updated == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "Application not found.")
            );
        }

        return ResponseEntity.ok(
                Map.of("message", "Application status updated successfully.")
        );
    }
}
