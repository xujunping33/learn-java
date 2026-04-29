public class Person {
    private int id;
    private String name;
    private int age;

    public Person(int id, String name, int age) {
        this.id = id;
        setName(name);
        setAge(age);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public boolean setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        this.name = name.trim();
        return true;
    }

    public boolean setAge(int age) {
        if (age < 0 || age > 150) {
            return false;
        }
        this.age = age;
        return true;
    }

    @Override
    public String toString() {
        return "Person{id=" + id + ", name='" + name + "', age=" + age + "}";
    }
}

