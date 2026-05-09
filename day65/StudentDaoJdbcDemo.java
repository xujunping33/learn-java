import java.util.List;

public class StudentDaoJdbcDemo {
    public static void main(String[] args) throws Exception {
        StudentDaoJdbc dao = new StudentDaoJdbc();

        long newId = dao.add(new Student(0, "jdbc_" + System.currentTimeMillis(), 88, 18));
        System.out.println("Inserted id = " + newId);

        System.out.println("findById(" + newId + "): " + dao.findById(newId).orElse(null));

        int updated = dao.updateScore(newId, 95);
        System.out.println("updateScore rows = " + updated);
        System.out.println("after update: " + dao.findById(newId).orElse(null));

        List<Student> page1 = dao.listPage(1, 5);
        System.out.println("listPage(1,5) size=" + page1.size());
        for (Student s : page1) {
            System.out.println("  " + s);
        }

        int deleted = dao.deleteById(newId);
        System.out.println("deleteById rows = " + deleted);
        System.out.println("after delete findById: " + dao.findById(newId).orElse(null));
    }
}

