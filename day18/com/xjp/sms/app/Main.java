package com.xjp.sms.app;

import com.xjp.sms.model.Student;
import com.xjp.sms.service.StudentService;
import com.xjp.sms.util.SafeInput;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);
        StudentService service = new StudentService();

        while (true) {
            printMenu();
            int choice = in.readIntInRange("请选择：", 0, 6);
            switch (choice) {
                case 0 -> {
                    System.out.println("退出。");
                    scanner.close();
                    return;
                }
                case 1 -> addStudent(in, service);
                case 2 -> updateStudent(in, service);
                case 3 -> deleteStudent(in, service);
                case 4 -> queryStudent(in, service);
                case 5 -> listStudents(service);
                case 6 -> showStats(service);
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== StudentScoreManager（package版）===");
        System.out.println("1. 新增学生");
        System.out.println("2. 修改学生（姓名/成绩）");
        System.out.println("3. 删除学生");
        System.out.println("4. 查询学生");
        System.out.println("5. 列表展示");
        System.out.println("6. 统计分析");
        System.out.println("0. 退出");
    }

    private static void addStudent(SafeInput in, StudentService service) {
        int id = in.readInt("输入 id（整数，需唯一）：");
        String name = in.readNonEmptyLine("输入 name：");
        int score = in.readIntInRange("输入 score（0-100）：", 0, 100);

        boolean ok = service.addStudent(id, name, score);
        if (ok) System.out.println("新增成功。");
        else System.out.println("新增失败：id 已存在。");
    }

    private static void updateStudent(SafeInput in, StudentService service) {
        int id = in.readInt("输入要修改的学生 id：");
        Student s = service.findById(id);
        if (s == null) {
            System.out.println("未找到该学生。");
            return;
        }

        System.out.println("当前：" + s);
        System.out.println("1. 修改姓名");
        System.out.println("2. 修改成绩");
        System.out.println("0. 返回");
        int sub = in.readIntInRange("请选择：", 0, 2);
        switch (sub) {
            case 0 -> { return; }
            case 1 -> {
                String newName = in.readNonEmptyLine("输入新姓名：");
                service.updateName(id, newName);
                System.out.println("修改成功。");
            }
            case 2 -> {
                int newScore = in.readIntInRange("输入新成绩（0-100）：", 0, 100);
                service.updateScore(id, newScore);
                System.out.println("修改成功。");
            }
        }
    }

    private static void deleteStudent(SafeInput in, StudentService service) {
        int id = in.readInt("输入要删除的学生 id：");
        boolean ok = service.deleteById(id);
        if (ok) System.out.println("删除成功。");
        else System.out.println("删除失败：未找到该学生。");
    }

    private static void queryStudent(SafeInput in, StudentService service) {
        int id = in.readInt("输入要查询的学生 id：");
        Student s = service.findById(id);
        if (s == null) System.out.println("未找到该学生。");
        else System.out.println(s);
    }

    private static void listStudents(StudentService service) {
        System.out.println();
        System.out.println("=== 学生列表 ===");
        if (service.listAll().isEmpty()) {
            System.out.println("<empty>");
            return;
        }
        for (Student s : service.listAll()) {
            System.out.println(s);
        }
    }

    private static void showStats(StudentService service) {
        StudentService.Stats stats = service.stats();
        System.out.println();
        System.out.println("=== 统计分析 ===");
        if (stats == null) {
            System.out.println("暂无数据。");
            return;
        }
        System.out.println("总人数 = " + stats.total);
        System.out.println("平均分 = " + stats.avg);
        System.out.println("最高分学生 = " + stats.max);
        System.out.println("最低分学生 = " + stats.min);
        System.out.println("不及格人数(<60) = " + stats.failCount);
    }
}

