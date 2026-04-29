public class LogicalTruthTable {
    public static void main(String[] args) {
        boolean[] vals = {false, true};
        for (boolean p : vals) {
            for (boolean q : vals) {
                System.out.println("p=" + p + ", q=" + q
                        + " | p&&q=" + (p && q)
                        + ", p||q=" + (p || q)
                        + ", !p=" + (!p));
            }
        }
    }
}

