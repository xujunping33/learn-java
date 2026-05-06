package learn.java.bootsocial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import learn.java.bootsocial.model.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    int insertPost(Post post);

    /** 分页 + 可选筛选；SQL 不带 limit，由 MyBatis-Plus 分页插件追加。 */
    IPage<Post> pagePosts(
            Page<Post> page, @Param("keyword") String keyword, @Param("userId") Long userId);

    Post findDetailById(@Param("id") long id);
}
