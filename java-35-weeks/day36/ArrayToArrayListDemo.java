import java.util.ArrayList;

public class ArrayToArrayListDemo {
    public static void main(String[] args) {
        // 这是把“数组存成绩”改成“ArrayList存成绩”的版本（对应你之前数组统计题）
        ArrayList<Integer> scores = new ArrayList<>();
        scores.add(60);
        scores.add(59);
        scores.add(100);
        scores.add(88);
        scores.add(0);

        int max = scores.get(0);
        int min = scores.get(0);
        long sum = 0;
        int fail = 0;

        for (int x : scores) {
            if (x > max) max = x;
            if (x < min) min = x;
            sum += x;
            if (x < 60) fail++;
        }

        double avg = sum / (double) scores.size();
        System.out.println("scores = " + scores);
        System.out.println("max = " + max);
        System.out.println("min = " + min);
        System.out.println("avg = " + avg);
        System.out.println("fail = " + fail);
    }
}

