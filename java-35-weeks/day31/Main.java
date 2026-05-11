import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);
        StudentServiceV2 service = new StudentServiceV2();

        while (true) {
            printMenu();
            int choice = in.readInt("请选择：");

            try {
                switch (choice) {
                    case 0 -> {
                        System.out.println("退出。");
                        scanner.close();
                        return;
                    }
                    case 1 -> addStudent(in, service);
                    case 2 -> updateScore(in, service);
                    case 3 -> listStudents(service);
                    case 4 -> parseScoreChainDemo(in, service);
                    default -> System.out.println("菜单越界，请输入 0-4。");
                }
            } catch (ValidationException e) {
                System.out.println("业务错误：" + e.getMessage());
                if (e.getCause() != null) {
                    System.out.println("根因：" + e.getCause().getClass().getSimpleName()
                            + " - " + e.getCause().getMessage());
                }
            } catch (Exception e) {
                System.out.println("系统异常：" + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== StudentServiceV2 Demo ===");
        System.out.println("1. 新增学生");
        System.out.println("2. 修改成绩");
        System.out.println("3. 列表");
        System.out.println("4. 异常链演示（解析成绩）");
        System.out.println("0. 退出");
    }

    private static void addStudent(SafeInput in, StudentServiceV2 service) {
        int id = in.readInt("输入 id：");
        String name = in.readLine("输入 name：");
        int score = in.readInt("输入 score：");
        service.addStudent(id, name, score);
        System.out.println("新增成功。");
    }

    private static void updateScore(SafeInput in, StudentServiceV2 service) {
        int id = in.readInt("输入 id：");
        int score = in.readInt("输入新 score：");
        service.updateScore(id, score);
        System.out.println("修改成功。");
    }

    private static void listStudents(StudentServiceV2 service) {
        if (service.listAll().isEmpty()) {
            System.out.println("<empty>");
            return;
        }
        for (Student s : service.listAll()) {
            System.out.println(s);
        }
    }

    private static void parseScoreChainDemo(SafeInput in, StudentServiceV2 service) {
        String raw = in.readLine("输入一个“可能非法”的成绩字符串：");
        int score = service.parseScoreOrThrow(raw);
        System.out.println("解析成功，score = " + score);
    }
}

