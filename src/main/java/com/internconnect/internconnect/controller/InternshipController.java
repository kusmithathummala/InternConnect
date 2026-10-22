
package com.internconnect.internconnect.controller;

import com.internconnect.internconnect.model.Internship;
import com.internconnect.internconnect.repository.InternshipRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internships")
public class InternshipController {

    private final InternshipRepository repository;

    public InternshipController(InternshipRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Internship> getInternships(
            @RequestParam(required = false) String keyword) {

        return repository.findAll(keyword);
    }
}
