package learn.java.springmvcdemo.model;

/** 内存仓储使用的领域模型（非对外 JSON 契约）。 */
public record Student(long id, String name, int score) {}
