import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

public class DbUtilsStudentDaoDemo {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("day67/druid.properties")) {
            props.load(in);
        }
        DataSource ds = DruidDataSourceFactory.createDataSource(props);

        DbUtilsStudentDao dao = new DbUtilsStudentDao(ds);

        String name = "dbutils_" + System.currentTimeMillis();
        long newId = dao.add(new Student(0, name, 91, 19));
        System.out.println("inserted id = " + newId);

        List<Student> all = dao.listAll();
        System.out.println("listAll size = " + all.size());
        for (int i = 0; i < Math.min(5, all.size()); i++) {
            System.out.println("  " + all.get(i));
        }

        System.out.println("findById(" + newId + "): " + dao.findById(newId).orElse(null));

        int updated = dao.updateScore(newId, 99);
        System.out.println("updateScore rows = " + updated);
        System.out.println("after update: " + dao.findById(newId).orElse(null));

        int deleted = dao.deleteById(newId);
        System.out.println("deleteById rows = " + deleted);
        System.out.println("after delete: " + dao.findById(newId).orElse(null));
    }
}

