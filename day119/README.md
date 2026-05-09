# Day 119：可重复部署与复盘

- **流程**：**`mvn clean package`** → 删 **`$CATALINA_HOME/webapps/servlet-demo*`** → 拷 **`target/servlet-demo.war`** → **`catalina.sh start`** → 用浏览器或 **`curl`** 验 URL → **`stop`**。
- **做两遍**：证明不是「碰巧成功一次」；详见 **`../servlet-demo/README.md` → Day119**。
- **笔记**：**`../W17-notes.md` → Day119**。
