public class Student {
    private static int count = 0;

    private final int id;
    private final String name;

    public Student(String name) {
        this.id = IdGenerator.next();
        this.name = name;
        count++;
    }

    public static int getCount() {
        return count;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "'}";
    }
}

