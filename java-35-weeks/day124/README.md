# Day 124：前后端正则双保险

- **后端**：**`../servlet-demo/src/main/java/learn/java/servlet/validation/StudentValidation.java`** + **`StudentsApiServlet`** 中 **POST/PUT** 调用
- **前端**：**`../servlet-demo/src/main/webapp/static/students.html`** 中 **`validateClient`**
- **笔记**：**`../W18-notes.md` → Day 124`**

自测：故意填非法手机或分数 101 → 应先被前端拦截；用 **curl** 直接 POST 非法 JSON → 应 **400** + **`error`**。
