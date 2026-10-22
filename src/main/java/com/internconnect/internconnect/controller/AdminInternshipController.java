
package com.internconnect.internconnect.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/internships")
public class AdminInternshipController {

    private final JdbcTemplate jdbcTemplate;

    public AdminInternshipController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Check whether an admin is logged in.
    private boolean isAdmin(HttpSession session) {
        return session.getAttribute("adminId") != null;
    }

    // Return an unauthorized response.
    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("message", "Please log in as an administrator.")
        );
    }

    // Read a request field as trimmed text.
    private String text(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? "" : value.toString().trim();
    }

    // Convert an empty optional field to null.
    private String optional(Map<String, Object> data, String key) {
        String value = text(data, key);
        return value.isEmpty() ? null : value;
    }

    // Validate and parse the optional deadline.
    private LocalDate parseDeadline(Map<String, Object> data) {
        String deadlineText = text(data, "deadline");

        if (deadlineText.isEmpty()) {
            return null;
        }

        return LocalDate.parse(deadlineText);
    }

    // Validate common internship fields.
    private String validate(Map<String, Object> data) {
        String title = text(data, "title");
        String company = text(data, "company");

        if (title.isEmpty() || company.isEmpty()) {
            return "Internship title and company are required.";
        }

        if (title.length() > 150 || company.length() > 150) {
            return "Title and company must not exceed 150 characters.";
        }

        if (text(data, "location").length() > 100) {
            return "Location must not exceed 100 characters.";
        }

        if (text(data, "internshipType").length() > 50) {
            return "Internship type must not exceed 50 characters.";
        }

        if (text(data, "workMode").length() > 50) {
            return "Work mode must not exceed 50 characters.";
        }

        if (text(data, "duration").length() > 50) {
            return "Duration must not exceed 50 characters.";
        }

        if (text(data, "stipend").length() > 100) {
            return "Stipend must not exceed 100 characters.";
        }

        if (text(data, "skills").length() > 255) {
            return "Skills must not exceed 255 characters.";
        }

        return null;
    }

    // GET: View all internships.
    @GetMapping
    public ResponseEntity<?> getAll(HttpSession session) {

        if (!isAdmin(session)) {
            return unauthorized();
        }

        List<Map<String, Object>> internships =
                jdbcTemplate.queryForList(
                        "SELECT * FROM internships ORDER BY id DESC"
                );

        return ResponseEntity.ok(internships);
    }

    // POST: Add a new internship.
    @PostMapping
    public ResponseEntity<?> add(
            @RequestBody Map<String, Object> data,
            HttpSession session) {

        if (!isAdmin(session)) {
            return unauthorized();
        }

        String validationError = validate(data);

        if (validationError != null) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", validationError)
            );
        }

        LocalDate deadline;

        try {
            deadline = parseDeadline(data);
        } catch (DateTimeParseException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Enter a valid deadline in YYYY-MM-DD format.")
            );
        }

        jdbcTemplate.update(
                """
                INSERT INTO internships
                (title, company, location, internship_type, work_mode,
                 duration, stipend, skills, description, deadline)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                text(data, "title"),
                text(data, "company"),
                optional(data, "location"),
                optional(data, "internshipType"),
                optional(data, "workMode"),
                optional(data, "duration"),
                optional(data, "stipend"),
                optional(data, "skills"),
                optional(data, "description"),
                deadline == null ? null : Date.valueOf(deadline)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "Internship added successfully.")
        );
    }

    // PUT: Update an existing internship.
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable int id,
            @RequestBody Map<String, Object> data,
            HttpSession session) {

        if (!isAdmin(session)) {
            return unauthorized();
        }

        if (id <= 0) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Invalid internship ID.")
            );
        }

        String validationError = validate(data);

        if (validationError != null) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", validationError)
            );
        }

        LocalDate deadline;

        try {
            deadline = parseDeadline(data);
        } catch (DateTimeParseException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Enter a valid deadline in YYYY-MM-DD format.")
            );
        }

        int updated = jdbcTemplate.update(
                """
                UPDATE internships
                SET title = ?,
                    company = ?,
                    location = ?,
                    internship_type = ?,
                    work_mode = ?,
                    duration = ?,
                    stipend = ?,
                    skills = ?,
                    description = ?,
                    deadline = ?
                WHERE id = ?
                """,
                text(data, "title"),
                text(data, "company"),
                optional(data, "location"),
                optional(data, "internshipType"),
                optional(data, "workMode"),
                optional(data, "duration"),
                optional(data, "stipend"),
                optional(data, "skills"),
                optional(data, "description"),
                deadline == null ? null : Date.valueOf(deadline),
                id
        );

        if (updated == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "Internship not found.")
            );
        }

        return ResponseEntity.ok(
                Map.of("message", "Internship updated successfully.")
        );
    }

    // DELETE: Delete an internship only when it has no applications.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable int id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return unauthorized();
        }

        if (id <= 0) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Invalid internship ID.")
            );
        }

        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM internships WHERE id = ?",
                Integer.class,
                id
        );

        if (exists == null || exists == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "Internship not found.")
            );
        }

        Integer applicationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM applications WHERE internship_id = ?",
                Integer.class,
                id
        );

        if (applicationCount != null && applicationCount > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of(
                            "message",
                            "This internship has applications and cannot be deleted."
                    )
            );
        }

        jdbcTemplate.update(
                "DELETE FROM internships WHERE id = ?",
                id
        );

        return ResponseEntity.ok(
                Map.of("message", "Internship deleted successfully.")
        );
    }
}
