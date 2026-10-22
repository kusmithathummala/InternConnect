package com.internconnect.internconnect.repository;

import com.internconnect.internconnect.model.Admin;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Admin findByEmail(String email) {

        String sql = """
            SELECT id, full_name, email, password_hash
            FROM admins
            WHERE email = ?
            """;

        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> {
                Admin admin = new Admin();
                admin.setId(rs.getInt("id"));
                admin.setFullName(rs.getString("full_name"));
                admin.setEmail(rs.getString("email"));
                admin.setPassword(rs.getString("password_hash"));
                return admin;
            },
            email
        ).stream().findFirst().orElse(null);
    }
}