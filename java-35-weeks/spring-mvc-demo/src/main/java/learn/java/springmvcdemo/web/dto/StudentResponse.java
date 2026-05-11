package learn.java.springmvcdemo.web.dto;

/** GET / POST / PUT 返回的统一 JSON 形状。 */
public record StudentResponse(long id, String name, int score) {}
