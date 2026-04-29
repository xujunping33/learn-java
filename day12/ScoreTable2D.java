import java.util.Scanner;

public class ScoreTable2D {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SafeInput in = new SafeInput(scanner);

        int classes = 3;
        int studentsPerClass = 5;
        int[][] scores = new int[classes][studentsPerClass];

        System.out.println("=== ScoreTable2D ===");
        System.out.println("输入 " + classes + " 个班级，每班 " + studentsPerClass + " 人成绩（0-100）。");

        for (int c = 0; c < classes; c++) {
            System.out.println("-- 班级 " + (c + 1) + " --");
            for (int s = 0; s < studentsPerClass; s++) {
                scores[c][s] = in.readIntInRange("学生 " + (s + 1) + "：", 0, 100);
            }
        }

        System.out.println();
        printTable(scores);

        System.out.println();
        printClassAverages(scores);

        System.out.println();
        printTopScore(scores);

        scanner.close();
    }

    private static void printTable(int[][] scores) {
        System.out.println("成绩表：");
        for (int c = 0; c < scores.length; c++) {
            System.out.print("班级" + (c + 1) + ": ");
            for (int s = 0; s < scores[c].length; s++) {
                if (s > 0) System.out.print(" ");
                System.out.print(scores[c][s]);
            }
            System.out.println();
        }
    }

    private static void printClassAverages(int[][] scores) {
        System.out.println("每班平均分：");
        for (int c = 0; c < scores.length; c++) {
            long sum = 0;
            for (int x : scores[c]) sum += x;
            double avg = sum / (double) scores[c].length;
            System.out.println("班级" + (c + 1) + " 平均分 = " + avg);
        }
    }

    private static void printTopScore(int[][] scores) {
        int top = scores[0][0];
        int topClass = 0;
        int topStudent = 0;

        for (int c = 0; c < scores.length; c++) {
            for (int s = 0; s < scores[c].length; s++) {
                if (scores[c][s] > top) {
                    top = scores[c][s];
                    topClass = c;
                    topStudent = s;
                }
            }
        }

        System.out.println("全年级最高分 = " + top
                + "（班级 " + (topClass + 1) + "，学号 " + (topStudent + 1) + "）");
    }
}

