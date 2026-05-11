package learn.java.springcoredemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import learn.java.springcoredemo.model.Student;
import learn.java.springcoredemo.service.StudentService;

/** Week22 Day153：集成测试；前缀 {@code w153_} + 清理，可重复 {@code mvn test}。 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
class StudentServiceIntegrationTest {

    private static final String PREFIX = "w153_";

    @Autowired
    private StudentService studentService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanW153Rows() {
        jdbcTemplate.update("DELETE FROM student WHERE name LIKE ?", PREFIX + "%");
    }

    @Test
    void addStudent_insertsAndCanQueryById() {
        String name = PREFIX + "add_" + System.nanoTime();
        long id = studentService.addStudent(name, 77);
        Student s = studentService.getStudent(id).orElseThrow();
        assertEquals(name, s.name());
        assertEquals(77, s.score());
    }

    @Test
    void getStudent_returnsEmptyWhenMissing() {
        assertTrue(studentService.getStudent(Long.MAX_VALUE).isEmpty());
    }

    @Test
    void setScore_updatesPersistedRow() {
        String name = PREFIX + "score_" + System.nanoTime();
        long id = studentService.addStudent(name, 50);
        assertEquals(1, studentService.setScore(id, 88));
        Student s = studentService.getStudent(id).orElseThrow();
        assertEquals(88, s.score());
    }

    @Test
    void listStudents_includesInsertedRow() {
        String name = PREFIX + "list_" + System.nanoTime();
        long id = studentService.addStudent(name, 33);
        boolean found =
                studentService.listStudents().stream()
                        .anyMatch(st -> st.id() == id && st.name().equals(name));
        assertTrue(found, "listStudents should contain inserted row");
    }
}
