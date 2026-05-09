package learn.java.mybatis.mapper;

import learn.java.mybatis.model.Student;
import learn.java.mybatis.model.StudentQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StudentMapper {
    Student selectById(long id);

    /** 单参数：XML 里用 #{name} */
    Student selectByName(String name);

    /**
     * 多参数：建议用 @Param 给参数命名，XML 里用 #{min} / #{max}。
     * 不用 @Param 时只能用 param1/param2 或 arg0/arg1（可读性差，易踩坑）。
     */
    List<Student> selectByScoreRange(@Param("min") int min, @Param("max") int max);

    /** 对象参数：XML 里用 #{name} / #{score} / #{age}（对应 getter 属性名） */
    int insert(Student student);

    /** 动态条件：name 模糊、scoreMin/scoreMax、age（均为可选，见 XML where 标签） */
    List<Student> selectByCondition(StudentQuery query);

    /** 批量插入：XML 用 foreach 拼多行 VALUES；MySQL 支持单语句多行 insert */
    int insertBatch(@Param("list") List<Student> list);

    /** IN 查询：ids 为空时勿调用（会生成非法 SQL） */
    List<Student> selectByIds(@Param("ids") List<Long> ids);

    /** 批量删除：ids 为空时勿调用 */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 分页：{@code LIMIT offset, pageSize}，须配合稳定 {@code ORDER BY}（见 XML）。
     */
    List<Student> selectPage(@Param("offset") int offset, @Param("pageSize") int pageSize);
}

