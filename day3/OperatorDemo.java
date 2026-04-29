public class OperatorDemo {
    private static boolean mark(String label) {
        System.out.println("  调用 mark(" + label + ")");
        return true;
    }

    public static void main(String[] args) {
        System.out.println("== 1) ++/-- 前后缀 ==");
        int x = 5;
        System.out.println("初始 x = " + x);
        System.out.println("x++ 返回旧值: " + (x++));
        System.out.println("执行后 x = " + x);
        System.out.println("++x 返回新值: " + (++x));
        System.out.println("执行后 x = " + x);
        System.out.println("x-- 返回旧值: " + (x--));
        System.out.println("执行后 x = " + x);
        System.out.println("--x 返回新值: " + (--x));
        System.out.println("执行后 x = " + x);

        System.out.println();
        System.out.println("== 2) && / || 短路 ==");
        int a = 0;
        System.out.println("a = " + a);

        System.out.println("-- (a != 0) && (10 / a > 1) --");
        if (a != 0 && (10 / a > 1)) {
            System.out.println("进入分支");
        } else {
            System.out.println("未进入分支（且没有除 0）");
        }

        System.out.println("-- (a == 0) || mark(\"RHS\") --");
        boolean r1 = (a == 0) || mark("RHS");
        System.out.println("结果 r1 = " + r1 + "（右侧应被短路，不会调用 mark）");

        System.out.println("-- (a != 0) && mark(\"RHS\") --");
        boolean r2 = (a != 0) && mark("RHS");
        System.out.println("结果 r2 = " + r2 + "（右侧应被短路，不会调用 mark）");

        System.out.println("-- (a == 0) && mark(\"RHS\") --");
        boolean r3 = (a == 0) && mark("RHS");
        System.out.println("结果 r3 = " + r3 + "（右侧不会短路，会调用 mark）");

        System.out.println();
        System.out.println("== 3) 三目运算符 ?: ==");
        int score = 59;
        String pass = score >= 60 ? "及格" : "不及格";
        System.out.println("score=" + score + " => " + pass);
    }
}

