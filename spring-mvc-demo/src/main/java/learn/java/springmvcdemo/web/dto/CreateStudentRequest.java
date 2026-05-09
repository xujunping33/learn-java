package learn.java.springmvcdemo.web.dto;

/**
 * POST/PUT JSON 请求体（Day156）；校验可在 W24 接 Jakarta Validation。
 */
public record CreateStudentRequest(String name, int score) {}
