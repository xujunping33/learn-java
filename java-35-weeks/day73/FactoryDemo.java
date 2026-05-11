/**
 * Day73：工厂模式 Demo（简单工厂 vs 工厂方法）
 *
 * 目标：
 * - 简单工厂：一个工厂根据 type 返回不同实现
 * - 工厂方法：不同工厂创建不同产品（新增类型时减少修改范围）
 */
public class FactoryDemo {
    public static void main(String[] args) {
        System.out.println("=== Simple Factory ===");
        Payment p1 = PaymentSimpleFactory.create("alipay");
        p1.pay(100.0);

        Payment p2 = PaymentSimpleFactory.create("wechat");
        p2.pay(66.6);

        System.out.println();
        System.out.println("=== Factory Method ===");
        PaymentFactory f1 = new AliPayFactory();
        Payment a = f1.create();
        a.pay(200.0);

        PaymentFactory f2 = new WeChatPayFactory();
        Payment w = f2.create();
        w.pay(88.8);
    }
}

// =========================
// Product（产品接口与实现）
// =========================

interface Payment {
    void pay(double amount);
}

class AliPay implements Payment {
    @Override
    public void pay(double amount) {
        System.out.println("[AliPay] paid " + amount);
    }
}

class WeChatPay implements Payment {
    @Override
    public void pay(double amount) {
        System.out.println("[WeChatPay] paid " + amount);
    }
}

// =========================
// Simple Factory（简单工厂）
// =========================

class PaymentSimpleFactory {
    /**
     * 简单工厂的特点：
     * - 调用方不需要 new 具体实现
     * - 但新增一种支付类型时，需要修改这个工厂方法（if/switch 会增长）
     */
    public static Payment create(String type) {
        if (type == null) throw new IllegalArgumentException("type must not be null");
        return switch (type.trim().toLowerCase()) {
            case "alipay" -> new AliPay();
            case "wechat" -> new WeChatPay();
            default -> throw new IllegalArgumentException("Unknown payment type: " + type);
        };
    }
}

// =========================
// Factory Method（工厂方法）
// =========================

interface PaymentFactory {
    Payment create();
}

class AliPayFactory implements PaymentFactory {
    @Override
    public Payment create() {
        return new AliPay();
    }
}

class WeChatPayFactory implements PaymentFactory {
    @Override
    public Payment create() {
        return new WeChatPay();
    }
}

