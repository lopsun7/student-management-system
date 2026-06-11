package com.studentmanagement.serviceimpl;

import com.studentmanagement.exception.ResourceNotFoundException;
import com.studentmanagement.model.Student;
import com.studentmanagement.repository.StudentRepository;
import com.studentmanagement.service.StudentAsyncService;
import com.studentmanagement.service.StudentService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(StudentServiceImpl.class);

	private final StudentRepository studentRepository;
	private final StudentAsyncService studentAsyncService;

	public StudentServiceImpl(StudentRepository studentRepository, StudentAsyncService studentAsyncService) {
		this.studentRepository = studentRepository;
		this.studentAsyncService = studentAsyncService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Student> getAllStudents() {
		return studentRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Student> searchStudentsByCourse(String course) {
		return studentRepository.findByCourseContainingIgnoreCase(course);
	}

	@Override
	public Student createStudent(Student student) {
		prepareStudentForSave(student);
		Student savedStudent = studentRepository.save(student);
		studentAsyncService.logStudentCreated(savedStudent.getId(), savedStudent.getEmail());
		return savedStudent;
	}

	@Override
	@Transactional(readOnly = true)
	public Student getStudentById(Long id) {
		return studentRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
	}

	@Override
	public Student updateStudent(Long id, Student studentDetails) {
		Student student = getStudentById(id);
		prepareStudentForSave(studentDetails);
		student.setFirstName(studentDetails.getFirstName());
		student.setLastName(studentDetails.getLastName());
		student.setEmail(studentDetails.getEmail());
		student.setCourse(studentDetails.getCourse());
		return studentRepository.save(student);
	}

	@Override
	public Map<String, Boolean> deleteStudent(Long id) {
		Student student = getStudentById(id);
		studentRepository.delete(student);
		Map<String, Boolean> response = new HashMap<>();
		response.put("deleted", Boolean.TRUE);
		return response;
	}

	public void prepareStudentForSave(Student student) {
		LOGGER.info("DIRECT CALL -> StudentServiceImpl.prepareStudentForSave(..)");
		normalizeStudentData(student);
	}

	private void normalizeStudentData(Student student) {
		LOGGER.info("DIRECT CALL -> StudentServiceImpl.normalizeStudentData(..)");
		if (student.getFirstName() != null) {
			student.setFirstName(student.getFirstName().trim());
		}
		if (student.getLastName() != null) {
			student.setLastName(student.getLastName().trim());
		}
		if (student.getEmail() != null) {
			student.setEmail(student.getEmail().trim().toLowerCase());
		}
		if (student.getCourse() != null) {
			student.setCourse(student.getCourse().trim());
		}
	}
}
