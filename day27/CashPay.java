public class CashPay implements Payment {
    @Override
    public boolean pay(double amount) {
        if (amount <= 0) return false;
        System.out.println("现金收款成功，金额=" + amount);
        return true;
    }

    @Override
    public String name() {
        return "Cash";
    }
}

