import java.util.ArrayList;

public class StudentServiceV2 {
    private final ArrayList<Student> students = new ArrayList<>();

    public void addStudent(int id, String name, int score) {
        validateIdUnique(id);
        validateName(name);
        validateScore(score);
        students.add(new Student(id, name.trim(), score));
    }

    public void updateScore(int id, int newScore) {
        validateScore(newScore);
        Student s = findById(id);
        if (s == null) {
            throw new ValidationException("更新失败：学生 id 不存在 -> " + id);
        }
        s.setScore(newScore);
    }

    public Student findById(int id) {
        for (Student s : students) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    public ArrayList<Student> listAll() {
        return students;
    }

    public int parseScoreOrThrow(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            // 异常链：保留底层解析失败根因
            throw new ValidationException("成绩解析失败：\"" + raw + "\" 不是合法整数", e);
        }
    }

    private void validateIdUnique(int id) {
        if (findById(id) != null) {
            throw new ValidationException("新增失败：id 已存在 -> " + id);
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("新增/修改失败：name 不能为空");
        }
    }

    private void validateScore(int score) {
        if (score < 0 || score > 100) {
            throw new ValidationException("成绩越界：必须在 0-100，实际为 " + score);
        }
    }
}

