
package com.internconnect.internconnect.controller;

import com.internconnect.internconnect.model.Student;
import com.internconnect.internconnect.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final StudentRepository studentRepository;

    public ProfileController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // View the logged-in student's profile
    @GetMapping
    public ResponseEntity<?> getProfile(HttpSession session) {

        Object studentIdObject = session.getAttribute("studentId");

        if (!(studentIdObject instanceof Integer)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Please log in to view your profile.")
            );
        }

        int studentId = (Integer) studentIdObject;

        Student student = studentRepository.findById(studentId);

        if (student == null) {
            session.invalidate();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Your account could not be found. Please log in again.")
            );
        }

        // Return only profile information, never the password.
        return ResponseEntity.ok(
                Map.of(
                        "id", student.getId(),
                        "fullName", student.getFullName(),
                        "email", student.getEmail(),
                        "phone", student.getPhone() == null
                                ? "" : student.getPhone()
                )
        );
    }

    // Update the logged-in student's profile
    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody Student student,
            HttpSession session) {

        Object studentIdObject = session.getAttribute("studentId");

        if (!(studentIdObject instanceof Integer)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Please log in to update your profile.")
            );
        }

        if (student.getFullName() == null ||
                student.getFullName().trim().isEmpty()) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Full name is required.")
            );
        }

        String fullName = student.getFullName().trim();
        String phone = student.getPhone();

        if (fullName.length() > 100) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Name cannot exceed 100 characters.")
            );
        }

        if (phone != null) {
            phone = phone.trim();

            if (phone.length() > 15) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Phone number cannot exceed 15 characters.")
                );
            }

            if (!phone.isEmpty() &&
                    !phone.matches("^[0-9+() .-]{7,15}$")) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Enter a valid phone number.")
                );
            }

            if (phone.isEmpty()) {
                phone = null;
            }
        }

        int studentId = (Integer) studentIdObject;

        int updatedRows = studentRepository.updateProfile(
                studentId,
                fullName,
                phone
        );

        if (updatedRows == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "Profile not found.")
            );
        }

        return ResponseEntity.ok(
                Map.of("message", "Profile updated successfully.")
        );
    }
}
