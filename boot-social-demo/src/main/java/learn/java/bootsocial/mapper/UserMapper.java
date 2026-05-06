package learn.java.bootsocial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import learn.java.bootsocial.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    int insertUser(User user);

    User findById(@Param("id") long id);
}

