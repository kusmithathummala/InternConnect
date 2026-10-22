
package com.internconnect.internconnect.model;

public class Internship {

    private int id;
    private String title;
    private String company;
    private String location;
    private String internshipType;
    private String workMode;
    private String duration;
    private String stipend;
    private String skills;
    private String description;
    private String deadline;

    public Internship() {
    }

    public Internship(
            int id, String title, String company,
            String location, String internshipType,
            String workMode, String duration,
            String stipend, String skills,
            String description, String deadline) {

        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.internshipType = internshipType;
        this.workMode = workMode;
        this.duration = duration;
        this.stipend = stipend;
        this.skills = skills;
        this.description = description;
        this.deadline = deadline;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getInternshipType() { return internshipType; }
    public void setInternshipType(String internshipType) {
        this.internshipType = internshipType;
    }

    public String getWorkMode() { return workMode; }
    public void setWorkMode(String workMode) {
        this.workMode = workMode;
    }

    public String getDuration() { return duration; }
    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getStipend() { return stipend; }
    public void setStipend(String stipend) {
        this.stipend = stipend;
    }

    public String getSkills() { return skills; }
    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
}
