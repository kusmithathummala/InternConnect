package com.internconnect.internconnect.repository;

import com.internconnect.internconnect.model.Application;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ApplicationRepository {

    private final JdbcTemplate jdbcTemplate;

    public ApplicationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean internshipExists(int internshipId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM internships WHERE id = ?",
                Integer.class,
                internshipId
        );

        return count != null && count > 0;
    }

    public boolean hasApplied(int studentId, int internshipId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM applications
                WHERE student_id = ? AND internship_id = ?
                """,
                Integer.class,
                studentId,
                internshipId
        );

        return count != null && count > 0;
    }

    public void apply(int studentId, int internshipId) {
        jdbcTemplate.update(
                """
                INSERT INTO applications (student_id, internship_id)
                VALUES (?, ?)
                """,
                studentId,
                internshipId
        );
    }

    public List<Application> findByStudentId(int studentId) {
        String sql = """
            SELECT a.id, a.internship_id,
                   i.title AS internship_title,
                   i.company,
                   a.status,
                   DATE_FORMAT(a.applied_at, '%Y-%m-%d %H:%i:%s')
                       AS applied_at
            FROM applications a
            JOIN internships i ON i.id = a.internship_id
            WHERE a.student_id = ?
            ORDER BY a.applied_at DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Application(
                        rs.getInt("id"),
                        rs.getInt("internship_id"),
                        rs.getString("internship_title"),
                        rs.getString("company"),
                        rs.getString("status"),
                        rs.getString("applied_at")
                ), studentId);
    }
}