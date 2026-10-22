
package com.internconnect.internconnect.controller;

import com.internconnect.internconnect.model.Student;
import com.internconnect.internconnect.repository.StudentRepository;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataAccessException;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final StudentRepository studentRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AuthController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Student student) {
        System.out.println("REGISTER METHOD REACHED");
System.out.println("Name received: " + student.getFullName());
System.out.println("Email received: " + student.getEmail());
System.out.println("Password received: " + (student.getPassword() != null));

        if (student.getFullName() == null
                || student.getFullName().trim().isEmpty()
                || student.getEmail() == null
                || student.getPassword() == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Name, email and password are required.")
            );
        }

        String name = student.getFullName().trim();
        String email = student.getEmail().trim().toLowerCase();
        String password = student.getPassword();

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Enter a valid email address.")
            );
        }

        if (password.length() < 8 || password.length() > 72) {
            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            "Password must be between 8 and 72 characters.")
            );
        }

        if (studentRepository.emailExists(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message",
                            "An account with this email already exists.")
            );
        }

        Student newStudent = new Student();
        newStudent.setFullName(name);
        newStudent.setEmail(email);
        newStudent.setPhone(student.getPhone());

        // Hash the password before storing it.
        newStudent.setPassword(passwordEncoder.encode(password));

        

        try {
            studentRepository.save(newStudent);
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message",
                            "An account with this email already exists.")
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message",
                        "Registration successful. You can now log in.")
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Student student,
            HttpSession session) {

        if (student.getEmail() == null
                || student.getPassword() == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Email and password are required.")
            );
        }

        String email = student.getEmail().trim().toLowerCase();
        Student existingStudent = studentRepository.findByEmail(email);

        if (existingStudent == null
                || !passwordEncoder.matches(
                        student.getPassword(),
                        existingStudent.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Invalid email or password.")
            );
        }

        // Store only the student's ID in the server-side session.
        session.setAttribute("studentId", existingStudent.getId());

        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful.",
                        "id", existingStudent.getId(),
                        "fullName", existingStudent.getFullName(),
                        "email", existingStudent.getEmail()
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully.")
        );
    }
}
