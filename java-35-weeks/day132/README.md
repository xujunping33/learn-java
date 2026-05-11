# Day132：提交 + 审批 + `leave_actions` + 事务

## 交付

- **`LeaveSubmitServlet`**：**`POST /api/leaves`**，JSON：`leaveType`（**`ANNUAL|SICK|OTHER`**）、`startAt` / `endAt`（**`LocalDateTime` 字符串**）、`reason`。
- **`LeaveDecisionServlet`**：**`POST .../approve`**、**`POST .../reject`**（驳回体 **`remark`**）。
- 主表与 **`leave_actions`** 同事务；审批用 **`version`** 条件更新挡重复操作。

## 验收

- 见 **`oa-demo/README.md`** curl 链；数据库中 **`leave_actions`** 应对 **`SUBMIT` / `APPROVE` / `REJECT`** 各能对应到行。
