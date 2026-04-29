public class IncInExpression {
    public static void main(String[] args) {
        int x = 3;
        int y = x++ + ++x; // 先用 x(3)，再 x=4；再 ++x 变 5；y = 3 + 5
        System.out.println("初始 x=3");
        System.out.println("y = x++ + ++x => y=" + y);
        System.out.println("最终 x=" + x);
    }
}

