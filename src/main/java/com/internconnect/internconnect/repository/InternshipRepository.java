
package com.internconnect.internconnect.repository;

import com.internconnect.internconnect.model.Internship;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InternshipRepository {

    private final JdbcTemplate jdbcTemplate;

    public InternshipRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Internship> findAll(String keyword) {

        String sql = """
            SELECT id, title, company, location,
                   internship_type, work_mode, duration,
                   stipend, skills, description,
                   DATE_FORMAT(deadline, '%Y-%m-%d') AS deadline
            FROM internships
            WHERE title LIKE ?
               OR company LIKE ?
               OR location LIKE ?
               OR skills LIKE ?
            ORDER BY created_at DESC, id DESC
            """;

        String search = "%" +
                (keyword == null ? "" : keyword.trim()) + "%";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
            new Internship(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("company"),
                rs.getString("location"),
                rs.getString("internship_type"),
                rs.getString("work_mode"),
                rs.getString("duration"),
                rs.getString("stipend"),
                rs.getString("skills"),
                rs.getString("description"),
                rs.getString("deadline")
            ),
            search, search, search, search
        );
    }
}
