package learn.java.bootsocial.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "评论正文")
public record CreateCommentRequest(@NotBlank @Size(max = 2000) String content) {}
