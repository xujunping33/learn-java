import java.util.List;
import java.util.Scanner;

/**
 * Day70：StudentScoreManager（数据库版最小可用）
 *
 * 结构职责：
 * - Main：交互/菜单，不写 SQL
 * - Service：业务校验与流程编排
 * - DAO：SQL 与 JDBC 细节
 */
public class Main {
    public static void main(String[] args) {
        StudentService service = new StudentService(createDao(args));

        try (Scanner scanner = new Scanner(System.in)) {
            SafeInput in = new SafeInput(scanner);
            while (true) {
                printMenu();
                int choice = in.readInt("请选择：");
                try {
                    switch (choice) {
                        case 0 -> {
                            System.out.println("退出。");
                            return;
                        }
                        case 1 -> add(in, service);
                        case 2 -> updateScore(in, service);
                        case 3 -> delete(in, service);
                        case 4 -> findById(in, service);
                        case 5 -> listAll(service);
                        default -> System.out.println("菜单越界：0-5");
                    }
                } catch (Exception e) {
                    // 这里统一兜底，避免程序因一次错误输入/SQL 异常直接退出
                    System.out.println("操作失败：" + e.getMessage());
                }
            }
        }
    }

    /**
     * 允许你在 JDBC/DBUtils 两种实现之间切换。
     *
     * - 默认：DBUtils（更少模板代码）
     * - 传参 "jdbc"：强制使用纯 JDBC 版
     */
    private static StudentDao createDao(String[] args) {
        boolean useJdbc = args != null && args.length > 0 && "jdbc".equalsIgnoreCase(args[0]);
        if (useJdbc) {
            System.out.println("[DAO] Using JDBC implementation.");
            return new StudentDaoJdbc();
        }
        try {
            System.out.println("[DAO] Using DBUtils implementation.");
            return new DbUtilsStudentDao();
        } catch (Exception e) {
            // 如果连接池配置有问题，退回 JDBC 版，确保你还能继续练习
            System.out.println("[DAO] DBUtils init failed, fallback to JDBC: " + e.getMessage());
            return new StudentDaoJdbc();
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== StudentScoreManager DB ===");
        System.out.println("1) 新增学生");
        System.out.println("2) 修改成绩");
        System.out.println("3) 删除学生");
        System.out.println("4) 按 id 查询");
        System.out.println("5) 列表展示");
        System.out.println("0) 退出");
    }

    private static void add(SafeInput in, StudentService service) throws Exception {
        String name = in.readLine("name：");
        int score = in.readInt("score(0-100)：");
        int age = in.readInt("age(0-120)：");
        long id = service.addStudent(name, score, age);
        System.out.println("新增成功，id=" + id);
    }

    private static void updateScore(SafeInput in, StudentService service) throws Exception {
        long id = in.readLong("id：");
        int score = in.readInt("new score(0-100)：");
        boolean ok = service.updateScore(id, score);
        System.out.println(ok ? "修改成功。" : "未找到该 id。");
    }

    private static void delete(SafeInput in, StudentService service) throws Exception {
        long id = in.readLong("id：");
        boolean ok = service.deleteById(id);
        System.out.println(ok ? "删除成功。" : "未找到该 id。");
    }

    private static void findById(SafeInput in, StudentService service) throws Exception {
        long id = in.readLong("id：");
        System.out.println(service.findById(id).map(Object::toString).orElse("<not found>"));
    }

    private static void listAll(StudentService service) throws Exception {
        List<Student> list = service.listAll();
        if (list.isEmpty()) {
            System.out.println("<empty>");
            return;
        }
        for (Student s : list) {
            System.out.println(s);
        }
    }
}

