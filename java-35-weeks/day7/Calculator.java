public class Calculator {
    public double add(double a, double b) {
        return a + b;
    }

    public double sub(double a, double b) {
        return a - b;
    }

    public double mul(double a, double b) {
        return a * b;
    }

    public Double div(double a, double b) {
        if (b == 0.0) return null;
        return a / b;
    }
}

