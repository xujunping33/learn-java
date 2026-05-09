package learn.java.ssmsocial.mapper;

import org.apache.ibatis.annotations.Param;

public interface PostLikeMapper {

    int insertLike(@Param("postId") long postId, @Param("userId") long userId);

    int deleteLike(@Param("postId") long postId, @Param("userId") long userId);

    long countLikes(@Param("postId") long postId);
}

