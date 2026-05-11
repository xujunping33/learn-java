package learn.java.mybatis.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Employee {
    private Long id;
    private String name;
    private BigDecimal baseSalary;
    private Long deptId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 关联对象：多表查询时由 resultMap + association 填充 */
    private Department department;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", baseSalary=" + baseSalary +
                ", deptId=" + deptId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", department=" + department +
                '}';
    }
}
