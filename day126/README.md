# Day 126：Filter 链 + 日志 + Session 鉴权

- **Filter**：**`../servlet-demo/src/main/java/learn/java/servlet/filter/RequestLoggingFilter.java`**、**`SimpleAuthFilter.java`**
- **登录 API**：**`../servlet-demo/src/main/java/learn/java/servlet/ApiLoginServlet.java`**
- **页面**：**`../servlet-demo/src/main/webapp/static/login.html`**
- **注册与顺序**：**`../servlet-demo/src/main/webapp/WEB-INF/web.xml`**
- **笔记**：**`../W18-notes.md` → Day 126`**

验收：未登录打开 **`static/students.html`** 应 **401** 提示；**`static/login.html`** 登录（口令 **demo**）后再打开学生页应能 **GET/POST/PUT/DELETE**。
