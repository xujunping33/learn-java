package learn.java.bootsocial.mapper;

import java.util.List;

import learn.java.bootsocial.model.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {

    int insertComment(Comment comment);

    List<Comment> listCommentsByPostId(@Param("postId") long postId);
}
