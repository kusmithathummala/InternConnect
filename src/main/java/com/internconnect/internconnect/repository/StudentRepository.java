
package com.internconnect.internconnect.repository;

import com.internconnect.internconnect.model.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepository {

    private final JdbcTemplate jdbcTemplate;

    public StudentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Check whether an email is already registered
    public boolean emailExists(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM students WHERE email = ?",
                Integer.class,
                email
        );

        return count != null && count > 0;
    }

    // Register a new student
    public void save(Student student) {
        String sql = """
            INSERT INTO students (full_name, email, password_hash, phone)
            VALUES (?, ?, ?, ?)
            """;

        jdbcTemplate.update(
                sql,
                student.getFullName(),
                student.getEmail(),
                student.getPassword(),
                student.getPhone()
        );
    }

    // Find a student by email for login
    public Student findByEmail(String email) {
        String sql = """
            SELECT id, full_name, email, password_hash, phone
            FROM students
            WHERE email = ?
            """;

        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) {
                return null;
            }

            Student student = new Student();
            student.setId(rs.getInt("id"));
            student.setFullName(rs.getString("full_name"));
            student.setEmail(rs.getString("email"));
            student.setPassword(rs.getString("password_hash"));
            student.setPhone(rs.getString("phone"));

            return student;
        }, email);
    }

    // Get profile details using the logged-in student's ID
    public Student findById(int studentId) {
        String sql = """
            SELECT id, full_name, email, phone
            FROM students
            WHERE id = ?
            """;

        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) {
                return null;
            }

            Student student = new Student();
            student.setId(rs.getInt("id"));
            student.setFullName(rs.getString("full_name"));
            student.setEmail(rs.getString("email"));
            student.setPhone(rs.getString("phone"));

            // Password is deliberately not retrieved for the profile.
            return student;
        }, studentId);
    }

    // Update only the name and phone number
    public int updateProfile(int studentId, String fullName, String phone) {
        String sql = """
            UPDATE students
            SET full_name = ?, phone = ?
            WHERE id = ?
            """;

        return jdbcTemplate.update(
                sql,
                fullName,
                phone,
                studentId
        );
    }
}
