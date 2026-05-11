package learn.java.mybatis.mapper;

import learn.java.mybatis.model.Employee;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmployeeMapper {
    /** JOIN department，Employee.department 嵌套映射 */
    Employee selectWithDeptById(long id);

    /** IN 查询：deptIds 为空时 XML 应不传（此处由调用方保证非空，见 Day82 foreach） */
    List<Employee> selectByDeptIds(@Param("deptIds") List<Long> deptIds);
}
