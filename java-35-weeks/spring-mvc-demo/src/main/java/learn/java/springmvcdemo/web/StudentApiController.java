package learn.java.springmvcdemo.web;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import learn.java.springmvcdemo.model.Student;
import learn.java.springmvcdemo.web.dto.CreateStudentRequest;
import learn.java.springmvcdemo.web.dto.StudentResponse;
import learn.java.springmvcdemo.web.exception.ApiException;

@RestController
@RequestMapping("/api/students")
public class StudentApiController {

    private final Map<Long, Student> store = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong();

    public StudentApiController() {
        store.put(1L, new Student(1L, "alice", 88));
        store.put(2L, new Student(2L, "bob", 76));
        nextId.set(2L);
    }

    /**
     * 列表；可选查询 {@code GET /api/students?name=xx}（不区分大小写，子串匹配 {@code Student#name()}）。
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StudentResponse> list(@RequestParam(name = "name", required = false) String nameFilter) {
        Stream<Student> stream =
                store.values().stream().sorted(Comparator.comparingLong(Student::id));
        if (nameFilter != null && !nameFilter.isBlank()) {
            String needle = nameFilter.trim().toLowerCase();
            stream = stream.filter(s -> s.name().toLowerCase().contains(needle));
        }
        return stream.map(this::toResponse).toList();
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public StudentResponse getOne(@PathVariable("id") long id) {
        Student s = store.get(id);
        if (s == null) {
            throw ApiException.notFound("student id=" + id);
        }
        return toResponse(s);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StudentResponse> create(@RequestBody CreateStudentRequest body) {
        String name = requireName(body);
        long id = nextId.incrementAndGet();
        Student created = new Student(id, name, body.score());
        store.put(id, created);
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(toResponse(created));
    }

    @PutMapping(
            path = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public StudentResponse replace(@PathVariable("id") long id, @RequestBody CreateStudentRequest body) {
        if (!store.containsKey(id)) {
            throw ApiException.notFound("student id=" + id);
        }
        String name = requireName(body);
        Student updated = new Student(id, name, body.score());
        store.put(id, updated);
        return toResponse(updated);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") long id) {
        if (store.remove(id) == null) {
            throw ApiException.notFound("student id=" + id);
        }
    }

    private static String requireName(CreateStudentRequest body) {
        if (body.name() == null || body.name().isBlank()) {
            throw ApiException.badRequest("name is required");
        }
        return body.name().trim();
    }

    private StudentResponse toResponse(Student s) {
        return new StudentResponse(s.id(), s.name(), s.score());
    }
}
