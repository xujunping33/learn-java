# W16 · 仿站首页 + Vue3 学生管理

本目录（`web/w16/`）包含两个可演示入口：**静态仿站首页**与 **Vite + Vue3 工程**。下面命令默认先 **`cd` 到 `web/w16`**。

## 仿站首页（`mukelike-home/`）

**方式一：** 用浏览器直接打开文件：

`web/w16/mukelike-home/index.html`

**方式二：** 在本机起一个静态目录（避免个别环境下相对路径差异）：

```bash
cd mukelike-home
python -m http.server 8080
```

浏览器访问：`http://localhost:8080`

## Vue 学生管理（`vue-student-admin/`）

首次安装依赖并启动开发服务器：

```bash
cd vue-student-admin
npm install
npm run dev
```

终端会打印本地地址（一般为 `http://localhost:5173`）。顶部可切换 **首页**（统计卡片）与 **学生管理**（表单 + 表格）。

生产构建与预览：

```bash
npm run build
npm run preview
```

## 口述备忘（过关用）

Vue + Vite 更适合中大型页面：**组件边界清晰**、**响应式状态集中好维护**、**工程化**（模块、构建、热更新）降低协作与迭代成本；纯 HTML/JS 在小页面够用，大了容易出现全局脚本耦合与 DOM 手工同步负担。
