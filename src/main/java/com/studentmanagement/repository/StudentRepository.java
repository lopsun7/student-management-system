package com.studentmanagement.repository;

import com.studentmanagement.model.Student;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepository {

	private static final RowMapper<Student> STUDENT_ROW_MAPPER = (rs, rowNum) -> {
		Student student = new Student();
		student.setId(rs.getLong("id"));
		student.setFirstName(rs.getString("first_name"));
		student.setLastName(rs.getString("last_name"));
		student.setEmail(rs.getString("email"));
		student.setCourse(rs.getString("course"));
		return student;
	};

	private final JdbcTemplate jdbcTemplate;

	public StudentRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Student> findAll() {
		return jdbcTemplate.query(
			"SELECT id, first_name, last_name, email, course FROM students ORDER BY id",
			STUDENT_ROW_MAPPER
		);
	}

	public Optional<Student> findById(Long id) {
		List<Student> students = jdbcTemplate.query(
			"SELECT id, first_name, last_name, email, course FROM students WHERE id = ?",
			STUDENT_ROW_MAPPER,
			id
		);
		return students.stream().findFirst();
	}

	public List<Student> findByCourseContainingIgnoreCase(String course) {
		return jdbcTemplate.query(
			"SELECT id, first_name, last_name, email, course FROM students WHERE LOWER(course) LIKE LOWER(?) ORDER BY id",
			STUDENT_ROW_MAPPER,
			"%" + course + "%"
		);
	}

	public Student save(Student student) {
		if (student.getId() == null) {
			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(connection -> {
				PreparedStatement statement = connection.prepareStatement(
					"INSERT INTO students (first_name, last_name, email, course) VALUES (?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS
				);
				statement.setString(1, student.getFirstName());
				statement.setString(2, student.getLastName());
				statement.setString(3, student.getEmail());
				statement.setString(4, student.getCourse());
				return statement;
			}, keyHolder);

			Number generatedId = keyHolder.getKey();
			if (generatedId != null) {
				student.setId(generatedId.longValue());
			}
			return student;
		}

		jdbcTemplate.update(
			"UPDATE students SET first_name = ?, last_name = ?, email = ?, course = ? WHERE id = ?",
			student.getFirstName(),
			student.getLastName(),
			student.getEmail(),
			student.getCourse(),
			student.getId()
		);
		return student;
	}

	public void delete(Student student) {
		jdbcTemplate.update("DELETE FROM students WHERE id = ?", student.getId());
	}

	public void deleteAll() {
		jdbcTemplate.update("DELETE FROM students");
	}
}
