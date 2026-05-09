package learn.java.ssmsocial.mapper;

import java.util.List;

import learn.java.ssmsocial.model.Post;
import org.apache.ibatis.annotations.Param;

public interface PostMapper {

    int insertPost(Post post);

    List<Post> listPosts(@Param("limit") int limit, @Param("offset") int offset);

    Post findDetailById(long id);
}

