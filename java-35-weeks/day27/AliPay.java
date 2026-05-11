public class AliPay implements Payment {
    @Override
    public boolean pay(double amount) {
        if (amount <= 0) return false;
        System.out.println("AliPay 支付成功，金额=" + amount);
        return true;
    }

    @Override
    public String name() {
        return "AliPay";
    }
}

