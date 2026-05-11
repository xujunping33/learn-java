# Day 128：OA 库表设计

- **建表脚本**：**`../sql/oa_schema.sql`**
- **需求对照**：**`../oa-demo/docs/requirements.md`**
- **笔记**：**`../W19-notes.md` → Day 128`**

导入示例（在仓库根 **`learn/java/`** 执行；库需已存在 **`oa_demo`**）：

```bash
# Ubuntu 上 root 常为 auth_socket，若 mysql -u root -p 报 1698，用：
sudo mysql -e "CREATE DATABASE IF NOT EXISTS oa_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
sudo mysql oa_demo < sql/oa_schema.sql
```
