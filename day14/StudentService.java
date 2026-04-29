import java.util.ArrayList;

public class StudentService {
    private final ArrayList<Student> students = new ArrayList<>();

    public boolean addStudent(int id, String name, int score) {
        if (findById(id) != null) return false;
        students.add(new Student(id, name, score));
        return true;
    }

    public boolean updateScore(int id, int newScore) {
        Student s = findById(id);
        if (s == null) return false;
        s.setScore(newScore);
        return true;
    }

    public boolean updateName(int id, String newName) {
        Student s = findById(id);
        if (s == null) return false;
        s.setName(newName);
        return true;
    }

    public boolean deleteById(int id) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getId() == id) {
                students.remove(i);
                return true;
            }
        }
        return false;
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

    public Stats stats() {
        if (students.isEmpty()) return null;

        long sum = 0;
        Student max = students.get(0);
        Student min = students.get(0);
        int fail = 0;

        for (Student s : students) {
            int score = s.getScore();
            sum += score;
            if (score > max.getScore()) max = s;
            if (score < min.getScore()) min = s;
            if (score < 60) fail++;
        }

        double avg = sum / (double) students.size();
        return new Stats(avg, max, min, fail, students.size());
    }

    public static class Stats {
        public final double avg;
        public final Student max;
        public final Student min;
        public final int failCount;
        public final int total;

        public Stats(double avg, Student max, Student min, int failCount, int total) {
            this.avg = avg;
            this.max = max;
            this.min = min;
            this.failCount = failCount;
            this.total = total;
        }
    }
}

