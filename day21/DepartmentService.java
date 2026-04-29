import java.util.ArrayList;

public class DepartmentService {
    private final ArrayList<Employee> employees = new ArrayList<>();

    public boolean add(Employee e) {
        if (findById(e.getId()) != null) return false;
        employees.add(e);
        return true;
    }

    public boolean deleteById(int id) {
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getId() == id) {
                employees.remove(i);
                return true;
            }
        }
        return false;
    }

    public Employee findById(int id) {
        for (Employee e : employees) {
            if (e.getId() == id) return e;
        }
        return null;
    }

    public ArrayList<Employee> listAll() {
        return employees;
    }
}

