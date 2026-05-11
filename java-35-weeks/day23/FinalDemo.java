public class FinalDemo {
    public static void main(String[] args) {
        final double TAX_RATE = 0.13;
        double price = 100;
        double total = price * (1 + TAX_RATE);
        System.out.println("price=" + price + ", TAX_RATE=" + TAX_RATE + ", total=" + total);

        int x = 120;
        int clamped = MathUtil.clamp(x, 0, 100);
        System.out.println("clamp(" + x + ",0,100) = " + clamped);

        final int[] arr = {1, 2, 3};
        arr[0] = 99; // final 让引用不可变，但对象内容仍可变
        System.out.println("final array first = " + arr[0]);
    }
}

