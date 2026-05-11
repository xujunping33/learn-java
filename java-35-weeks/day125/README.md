# Day 125：EncodingFilter

- **类**：**`../servlet-demo/src/main/java/learn/java/servlet/filter/EncodingFilter.java`**
- **注册**：**`../servlet-demo/src/main/webapp/WEB-INF/web.xml`**（**`/*`**，链上靠前）
- **笔记**：**`../W18-notes.md` → Day 125`**

验收：部署后 **`EchoServlet` POST 中文**、**`/api/students` JSON 中文**、静态 **HTML** 在 Network 里 **`Content-Type`** 是否带 **`charset=UTF-8`**；Tomcat **`logs`** 中可见 **`EncodingFilter#init`**（及 **`destroy`** 在停服时）。
