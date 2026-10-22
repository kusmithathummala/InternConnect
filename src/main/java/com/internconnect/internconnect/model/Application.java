
package com.internconnect.internconnect.model;

public class Application {

    private int id;
    private int internshipId;
    private String internshipTitle;
    private String company;
    private String status;
    private String appliedAt;

    public Application() {
    }

    public Application(int id, int internshipId,
                       String internshipTitle, String company,
                       String status, String appliedAt) {
        this.id = id;
        this.internshipId = internshipId;
        this.internshipTitle = internshipTitle;
        this.company = company;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInternshipId() {
        return internshipId;
    }

    public void setInternshipId(int internshipId) {
        this.internshipId = internshipId;
    }

    public String getInternshipTitle() {
        return internshipTitle;
    }

    public void setInternshipTitle(String internshipTitle) {
        this.internshipTitle = internshipTitle;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(String appliedAt) {
        this.appliedAt = appliedAt;
    }
}
