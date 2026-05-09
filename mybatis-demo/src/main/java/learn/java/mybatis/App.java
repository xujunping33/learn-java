package learn.java.mybatis;

import learn.java.mybatis.mapper.DepartmentMapper;
import learn.java.mybatis.mapper.EmployeeMapper;
import learn.java.mybatis.mapper.StudentMapper;
import learn.java.mybatis.model.PageRequest;
import learn.java.mybatis.model.Student;
import learn.java.mybatis.model.StudentQuery;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class App {
    public static void main(String[] args) throws Exception {
        // Day79：演示参数传递（单参 / 多参 @Param / 对象 insert）
        // 用法：
        //   mvn ...:java -Dexec.args="id 21"
        //   mvn ...:java -Dexec.args="name alice"
        //   mvn ...:java -Dexec.args="range 60 90"
        //   mvn ...:java -Dexec.args="insert mybatis_stu_1 88 18"
        // Day80：关联查询
        //   mvn ...:java -Dexec.args="emp 21"
        //   mvn ...:java -Dexec.args="dept 1"
        // Day81：动态 SQL / foreach IN
        //   mvn ...:java -Dexec.args="cond"                    （默认：score 60~90）
        //   mvn ...:java -Dexec.args="cond ali - - -"          （name 含 ali，- 表示不传该条件）
        //   mvn ...:java -Dexec.args="byDept 1,2,3"
        // Day82：foreach 批量插入 / IN 查询 / 批量删除
        //   mvn ...:java -Dexec.args="batch"
        //   mvn ...:java -Dexec.args="inIds 22,23,24"
        //   mvn ...:java -Dexec.args="delIds 999,998"   （慎用，会真删）
        // Day83：LIMIT 分页 + PageRequest
        //   mvn ...:java -Dexec.args="page"              （默认第 1 页、每页 5 条）
        //   mvn ...:java -Dexec.args="page 2 5"
        // Day84：一级缓存 + 日志见 CacheDemo（单独 main）
        //   mvn exec:java@cache-demo -Dexec.args="21"
        String mode = "id";
        if (args != null && args.length > 0) {
            mode = args[0];
        }

        // 1) 读取 MyBatis 总配置
        try (InputStream in = Resources.getResourceAsStream("mybatis-config.xml")) {
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);

            // 2) 打开会话（相当于一次 DB 会话/连接）
            try (SqlSession session = factory.openSession()) {
                // 3) 获取 Mapper（MyBatis 会在运行时生成代理对象）
                StudentMapper studentMapper = session.getMapper(StudentMapper.class);
                DepartmentMapper departmentMapper = session.getMapper(DepartmentMapper.class);
                EmployeeMapper employeeMapper = session.getMapper(EmployeeMapper.class);

                switch (mode) {
                    case "id" -> {
                        long id = args != null && args.length > 1 ? Long.parseLong(args[1]) : 1L;
                        Student s = studentMapper.selectById(id);
                        System.out.println("selectById(" + id + ") => " + s);
                    }
                    case "name" -> {
                        String name = args != null && args.length > 1 ? args[1] : "alice";
                        Student s = studentMapper.selectByName(name);
                        System.out.println("selectByName(" + name + ") => " + s);
                    }
                    case "range" -> {
                        int min = args != null && args.length > 2 ? Integer.parseInt(args[1]) : 60;
                        int max = args != null && args.length > 2 ? Integer.parseInt(args[2]) : 90;
                        var list = studentMapper.selectByScoreRange(min, max);
                        System.out.println("selectByScoreRange(" + min + "," + max + ") size=" + list.size());
                        list.forEach(System.out::println);
                    }
                    case "insert" -> {
                        String name = args != null && args.length > 3 ? args[1] : ("mb_" + System.currentTimeMillis());
                        int score = args != null && args.length > 3 ? Integer.parseInt(args[2]) : 88;
                        int age = args != null && args.length > 3 ? Integer.parseInt(args[3]) : 18;
                        Student s = new Student();
                        s.setName(name);
                        s.setScore(score);
                        s.setAge(age);
                        int rows = studentMapper.insert(s);
                        session.commit(); // insert 需要提交事务
                        System.out.println("insert rows=" + rows + ", generated id=" + s.getId());
                    }
                    case "emp" -> {
                        // 若库中 employee 最小 id 不是 1，可显式传参：emp 21
                        long empId = args != null && args.length > 1 ? Long.parseLong(args[1]) : 21L;
                        var e = employeeMapper.selectWithDeptById(empId);
                        System.out.println("selectWithDeptById(" + empId + ") => " + e);
                    }
                    case "dept" -> {
                        long deptId = args != null && args.length > 1 ? Long.parseLong(args[1]) : 1L;
                        var d = departmentMapper.selectById(deptId);
                        System.out.println("department.selectById(" + deptId + ") => " + d);
                    }
                    case "cond" -> {
                        StudentQuery q = new StudentQuery();
                        if (args != null && args.length > 1) {
                            if (!"-".equals(args[1])) {
                                q.setName(args[1]);
                            }
                            if (args.length > 2 && !"-".equals(args[2])) {
                                q.setScoreMin(Integer.parseInt(args[2]));
                            }
                            if (args.length > 3 && !"-".equals(args[3])) {
                                q.setScoreMax(Integer.parseInt(args[3]));
                            }
                            if (args.length > 4 && !"-".equals(args[4])) {
                                q.setAge(Integer.parseInt(args[4]));
                            }
                        } else {
                            q.setScoreMin(60);
                            q.setScoreMax(90);
                        }
                        var list = studentMapper.selectByCondition(q);
                        System.out.println("selectByCondition(" + qBrief(q) + ") size=" + list.size());
                        list.forEach(System.out::println);
                    }
                    case "byDept" -> {
                        String csv = args != null && args.length > 1 ? args[1] : "1,2";
                        List<Long> deptIds = parseLongCsv(csv);
                        var list = employeeMapper.selectByDeptIds(deptIds);
                        System.out.println("selectByDeptIds(" + deptIds + ") size=" + list.size());
                        list.forEach(System.out::println);
                    }
                    case "batch" -> {
                        long t = System.currentTimeMillis();
                        List<Student> batch = new ArrayList<>();
                        for (int i = 0; i < 5; i++) {
                            Student st = new Student();
                            st.setName("d82_" + t + "_" + i);
                            st.setScore(70 + i);
                            st.setAge(18);
                            batch.add(st);
                        }
                        int rows = studentMapper.insertBatch(batch);
                        session.commit();
                        System.out.println("insertBatch rows=" + rows);
                        batch.forEach(s -> System.out.println("  " + s));
                    }
                    case "inIds" -> {
                        String csv = args != null && args.length > 1 ? args[1] : "22,23,24";
                        List<Long> ids = parseLongCsv(csv);
                        if (ids.isEmpty()) {
                            System.out.println("inIds: need non-empty id list");
                            break;
                        }
                        var list = studentMapper.selectByIds(ids);
                        System.out.println("selectByIds(" + ids + ") size=" + list.size());
                        list.forEach(System.out::println);
                    }
                    case "delIds" -> {
                        if (args == null || args.length < 2) {
                            System.out.println("delIds: pass comma-separated ids, e.g. delIds 101,102");
                            break;
                        }
                        List<Long> ids = parseLongCsv(args[1]);
                        if (ids.isEmpty()) {
                            System.out.println("delIds: no ids parsed");
                            break;
                        }
                        int rows = studentMapper.deleteByIds(ids);
                        session.commit();
                        System.out.println("deleteByIds(" + ids + ") rows=" + rows);
                    }
                    case "page" -> {
                        int p = args != null && args.length > 2 ? Integer.parseInt(args[1]) : 1;
                        int sz = args != null && args.length > 2 ? Integer.parseInt(args[2]) : 5;
                        PageRequest pr = new PageRequest(p, sz);
                        var list = studentMapper.selectPage(pr.getOffset(), pr.getPageSize());
                        System.out.println("selectPage page=" + pr.getPage()
                                + " pageSize=" + pr.getPageSize()
                                + " offset=" + pr.getOffset()
                                + " => size=" + list.size());
                        list.forEach(System.out::println);
                    }
                    default -> System.out.println("Unknown mode: " + mode
                            + ". Use: id|name|range|insert|emp|dept|cond|byDept|batch|inIds|delIds|page");
                }
            }
        } finally {
            // exec:java 会等待非 daemon 线程退出；MySQL 驱动会启动 cleanup 线程，这里显式关闭避免 WARNING。
            try {
                Class<?> t = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
                t.getMethod("checkedShutdown").invoke(null);
            } catch (Throwable ignored) {
                // 不影响主流程：有些环境/版本可能没有该类或方法
            }
        }
    }

    private static String qBrief(StudentQuery q) {
        return "name=" + q.getName()
                + ", scoreMin=" + q.getScoreMin()
                + ", scoreMax=" + q.getScoreMax()
                + ", age=" + q.getAge();
    }

    private static List<Long> parseLongCsv(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
    }
}

