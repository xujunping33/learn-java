import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);
        DepartmentService service = new DepartmentService();

        while (true) {
            printMenu();
            int choice = in.readIntInRange("请选择：", 0, 5);
            switch (choice) {
                case 0 -> {
                    System.out.println("退出。");
                    scanner.close();
                    return;
                }
                case 1 -> addEmployee(in, service);
                case 2 -> deleteEmployee(in, service);
                case 3 -> queryEmployee(in, service);
                case 4 -> listEmployees(service);
                case 5 -> calcSalary(in, service);
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== DepartmentEmployeeManager ===");
        System.out.println("1. 新增员工（普通员工/经理）");
        System.out.println("2. 删除员工");
        System.out.println("3. 查询员工（按 id）");
        System.out.println("4. 列表展示");
        System.out.println("5. 计算工资（按 id）");
        System.out.println("0. 退出");
    }

    private static void addEmployee(SafeInput in, DepartmentService service) {
        System.out.println("1. 普通员工");
        System.out.println("2. 经理");
        int type = in.readIntInRange("选择类型：", 1, 2);

        int id = in.readInt("输入 id（整数，需唯一）：");
        if (service.findById(id) != null) {
            System.out.println("新增失败：id 已存在。");
            return;
        }

        String name = in.readNonEmptyLine("输入 name：").trim();
        double base = in.readDouble("输入 baseSalary：");

        Employee e;
        if (type == 1) {
            e = new Employee(id, name, base);
        } else {
            double bonus = in.readDouble("输入 bonus：");
            e = new Manager(id, name, base, bonus);
        }

        service.add(e);
        System.out.println("新增成功：" + e);
    }

    private static void deleteEmployee(SafeInput in, DepartmentService service) {
        int id = in.readInt("输入要删除的员工 id：");
        boolean ok = service.deleteById(id);
        if (ok) System.out.println("删除成功。");
        else System.out.println("删除失败：未找到该员工。");
    }

    private static void queryEmployee(SafeInput in, DepartmentService service) {
        int id = in.readInt("输入要查询的员工 id：");
        Employee e = service.findById(id);
        if (e == null) System.out.println("未找到该员工。");
        else System.out.println(e);
    }

    private static void listEmployees(DepartmentService service) {
        System.out.println();
        System.out.println("=== 员工列表 ===");
        if (service.listAll().isEmpty()) {
            System.out.println("<empty>");
            return;
        }
        for (Employee e : service.listAll()) {
            System.out.println(e);
        }
    }

    private static void calcSalary(SafeInput in, DepartmentService service) {
        int id = in.readInt("输入员工 id：");
        Employee e = service.findById(id);
        if (e == null) {
            System.out.println("未找到该员工。");
            return;
        }
        double salary = e.calcSalary(); // 多态：Employee/Manager 会走各自实现
        System.out.println("工资 = " + salary);
    }
}

