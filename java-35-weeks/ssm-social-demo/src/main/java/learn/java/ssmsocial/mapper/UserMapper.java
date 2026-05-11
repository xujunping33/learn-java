package learn.java.ssmsocial.mapper;

import learn.java.ssmsocial.model.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int insertUser(User user);

    User findByUsername(@Param("username") String username);

    User findById(@Param("id") long id);
}

