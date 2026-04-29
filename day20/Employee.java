public class Employee extends Person {
    private final double baseSalary;

    public Employee(int id, String name, double baseSalary) {
        super(id, name);
        this.baseSalary = baseSalary;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    @Override
    public String toString() {
        return "Employee{id=" + getId()
                + ", name='" + getName() + '\''
                + ", baseSalary=" + baseSalary
                + "}";
    }
}

