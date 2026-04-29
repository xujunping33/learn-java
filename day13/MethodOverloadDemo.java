public class MethodOverloadDemo {
    public static void main(String[] args) {
        System.out.println("sum(int,int) = " + sum(3, 4));
        System.out.println("sum(int,int,int) = " + sum(3, 4, 5));
        System.out.println("sum(double,double) = " + sum(1.2, 3.4));

        System.out.println("max(int,int) = " + max(7, 2));
        System.out.println("max(int,int,int) = " + max(7, 2, 9));
    }

    public static int sum(int a, int b) {
        return a + b;
    }

    public static int sum(int a, int b, int c) {
        return a + b + c;
    }

    public static double sum(double a, double b) {
        return a + b;
    }

    public static int max(int a, int b) {
        return a > b ? a : b;
    }

    public static int max(int a, int b, int c) {
        return max(max(a, b), c);
    }
}

