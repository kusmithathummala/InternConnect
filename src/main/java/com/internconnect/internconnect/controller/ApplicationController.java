package com.internconnect.internconnect.controller;

import com.internconnect.internconnect.model.Application;
import com.internconnect.internconnect.repository.ApplicationRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationRepository applicationRepository;

    public ApplicationController(
            ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @PostMapping("/{internshipId}")
    public ResponseEntity<?> apply(
            @PathVariable int internshipId,
            HttpSession session) {

        Object studentIdObject = session.getAttribute("studentId");

        if (!(studentIdObject instanceof Integer)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Please log in before applying.")
            );
        }

        int studentId = (Integer) studentIdObject;

        if (!applicationRepository.internshipExists(internshipId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "Internship not found.")
            );
        }

        if (applicationRepository.hasApplied(studentId, internshipId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message",
                            "You have already applied for this internship.")
            );
        }

        try {
            applicationRepository.apply(studentId, internshipId);
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message",
                            "You have already applied for this internship.")
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "Application submitted successfully!")
        );
    }

    @GetMapping("/my")
    public ResponseEntity<?> myApplications(HttpSession session) {

        Object studentIdObject = session.getAttribute("studentId");

        if (!(studentIdObject instanceof Integer)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message",
                            "Please log in to view your applications.")
            );
        }

        int studentId = (Integer) studentIdObject;

        List<Application> applications =
                applicationRepository.findByStudentId(studentId);

        return ResponseEntity.ok(applications);
    }
}



