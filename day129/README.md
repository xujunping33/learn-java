# Day129：初始化数据（角色 / 权限 / 演示账号）

## 交付

- **`sql/oa_seed.sql`**：部门、角色、权限、**`role_permissions`**、三个演示用户、**`user_roles`**、**`employees`**（含「员工 → 经理」上下级）。
- 导入顺序：**先** **`sql/oa_schema.sql`**，**再** **`sql/oa_seed.sql`**。
- 默认账号与口令见 **`oa-demo/README.md`**。

## 密码与幂等

- 算法：**`password_hash = MD5(UTF-8 明文密码 + salt)`** 的 **32 位小写十六进制**（与 `oa_seed.sql` 文件头一致）。
- 种子脚本可重复执行：角色/权限/部门与用户为 **UPSERT**；演示用户的 **`user_roles`** / **`employees`** 会先删再插；**`role_permissions`** 为 **`INSERT IGNORE`**。

## 本机 MySQL（Ubuntu `auth_socket`）

若 **`mysql -u root -p`** 报 **1698**，请用：

`sudo mysql oa_demo < sql/oa_seed.sql`
