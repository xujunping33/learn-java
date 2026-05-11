import java.util.List;
import java.util.Optional;

/**
 * DAO 接口：定义“数据访问能力”，隐藏具体实现（JDBC/DBUtils/MyBatis 都可以替换）。
 */
public interface StudentDao {
    long add(Student s) throws Exception;

    int updateScore(long id, int score) throws Exception;

    int deleteById(long id) throws Exception;

    Optional<Student> findById(long id) throws Exception;

    List<Student> listAll() throws Exception;
}

