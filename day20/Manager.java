public class Manager extends Employee {
    private final double bonus;

    public Manager(int id, String name, double baseSalary, double bonus) {
        super(id, name, baseSalary);
        this.bonus = bonus;
    }

    public double getBonus() {
        return bonus;
    }

    @Override
    public String toString() {
        return "Manager{id=" + getId()
                + ", name='" + getName() + '\''
                + ", baseSalary=" + getBaseSalary()
                + ", bonus=" + bonus
                + "}";
    }
}

