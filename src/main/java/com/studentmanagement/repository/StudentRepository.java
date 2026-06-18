package com.studentmanagement.repository;

import com.studentmanagement.model.Student;
import java.util.List;
import java.util.Optional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class StudentRepository {

	private final SessionFactory sessionFactory;

	public StudentRepository(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public List<Student> findAll() {
		return currentSession()
			.createQuery("from Student order by id", Student.class)
			.getResultList();
	}

	public Optional<Student> findById(Long id) {
		return Optional.ofNullable(currentSession().get(Student.class, id));
	}

	public List<Student> findByCourseContainingIgnoreCase(String course) {
		return currentSession()
			.createQuery(
				"from Student s where lower(s.course) like lower(:course) order by s.id",
				Student.class
			)
			.setParameter("course", "%" + course + "%")
			.getResultList();
	}

	public Student save(Student student) {
		if (student.getId() == null) {
			currentSession().persist(student);
			return student;
		}
		return (Student) currentSession().merge(student);
	}

	public void delete(Student student) {
		Session session = currentSession();
		Student managedStudent = session.contains(student) ? student : session.merge(student);
		session.remove(managedStudent);
	}

	public void deleteAll() {
		currentSession().createMutationQuery("delete from Student").executeUpdate();
	}

	private Session currentSession() {
		return sessionFactory.getCurrentSession();
	}
}
