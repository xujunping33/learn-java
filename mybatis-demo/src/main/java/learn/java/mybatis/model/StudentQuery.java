package learn.java.mybatis.model;

/**
 * 动态查询条件：字段均可为空，由 Mapper XML 的 if/where 决定是否拼进 SQL。
 */
public class StudentQuery {
    /** 非空时：name LIKE %...% */
    private String name;
    private Integer scoreMin;
    private Integer scoreMax;
    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getScoreMin() {
        return scoreMin;
    }

    public void setScoreMin(Integer scoreMin) {
        this.scoreMin = scoreMin;
    }

    public Integer getScoreMax() {
        return scoreMax;
    }

    public void setScoreMax(Integer scoreMax) {
        this.scoreMax = scoreMax;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
