package learn.java.bootsocial.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostLikeMapper {

    int insertLike(@Param("postId") long postId, @Param("userId") long userId);

    int deleteLike(@Param("postId") long postId, @Param("userId") long userId);
}
