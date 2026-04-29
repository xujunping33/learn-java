public class Student {
    public int id;
    public String name;
    public int score;

    public Student(int id, String name) {
        this(id, name, 0);
    }

    public Student(int id, String name, int score) {
        this.id = id;
        this.name = name;
        this.score = score;
    }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', score=" + score + "}";
    }
}

