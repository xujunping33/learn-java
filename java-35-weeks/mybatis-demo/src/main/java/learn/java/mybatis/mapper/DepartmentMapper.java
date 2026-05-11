package learn.java.mybatis.mapper;

import learn.java.mybatis.model.Department;

public interface DepartmentMapper {
    Department selectById(long id);
}
