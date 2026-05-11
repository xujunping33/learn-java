# Day136：动态 SQL 模板 + 数据范围（接口）

## 交付

- **`sql/oa_dynamic_queries.sql`**：注释型模板（本人 / 本部门 + 默认待审 / 时间窗 / RBAC EXISTS / 审计 / 分页），强调 **PreparedStatement** 与 **白名单**。
- **`LeaveQueryParams`** + **`LeavesMeServlet` / `LeavesPendingServlet`**：**`?status=`**、**`?leaveType=`**（枚举白名单）；非法值 **400**。

## 示例

```text
GET /oa-demo/api/leaves/me?status=SUBMITTED&leaveType=ANNUAL
GET /oa-demo/api/leaves/pending?status=APPROVED
```

（第二例不传 **`status`** 时仍为默认 **`SUBMITTED`** 待审。）

## 口述

- **经理查询必须带 dept 条件**：否则等价于扫全库请假单；**`dept_id` 子查询**把可见集合限制在**本人所在部门**。
