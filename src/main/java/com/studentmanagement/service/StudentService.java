package com.studentmanagement.service;

import com.studentmanagement.model.Student;
import java.util.List;
import java.util.Map;

public interface StudentService {

	List<Student> getAllStudents();

	Student createStudent(Student student);

	Student getStudentById(Long id);

	Student updateStudent(Long id, Student studentDetails);

	Map<String, Boolean> deleteStudent(Long id);
}
