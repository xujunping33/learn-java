package learn.java.ssmsocial.mapper;

import java.util.List;

import learn.java.ssmsocial.model.Comment;
import org.apache.ibatis.annotations.Param;

public interface CommentMapper {

    int insertComment(Comment comment);

    List<Comment> listCommentsByPostId(@Param("postId") long postId);
}

