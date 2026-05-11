package learn.java.springmvcdemo.web.dto;

/** Day157：404 等价错误体（Day158 可改为全局 `@RestControllerAdvice`）。 */
public record ApiErrorBody(String code, String message) {}
