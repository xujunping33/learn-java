## 第15周学习计划：DOM/BOM 进阶 + ES6 + 前端综合案例 + Vue3 入门

对应原路径：第15–16周《DOM/BOM/ES6/仿站/Vue3》中的 **前半段（DOM/BOM + ES6 + 综合案例 + Vue3入门）**。  
学习时长：每天约 2 小时。

本周核心目标
- DOM：事件委托、动态渲染、组件化思维（用模块拆分 JS）
- BOM：`localStorage`、页面生命周期、简单路由式页面切换（hash）
- ES6：解构、展开、默认参数、模板字符串、箭头函数、模块化（ESM）
- 综合案例：做一个“仿课程首页/学习平台首页”风格的落地页（纯静态资源即可）
- Vue3：用最小方式跑起来（CDN 版优先，避免构建工具卡你一周）

本周交付物（必须完成）
- `web/w15/landing/`：仿站综合案例（至少包含：导航、Banner、课程卡片网格、页脚）
- `web/w15/student-app/`：把你 `web/w13/` 的学生管理交互升级一版（更模块化）
  - `js/modules/storage.js`（封装 localStorage）
  - `js/modules/studentsModel.js`（数据层）
  - `js/pages/studentsPage.js`（页面逻辑）
- `web/w15/vue3-demo/index.html`：Vue3 CDN 最小示例（列表渲染 + 事件 + 计算属性）
- `W15-notes.md`

目录建议
- `day99` ~ `day105`

每天固定节奏（2小时）
- 20min：复盘（控制台无报错、关键交互再点一遍）
- 70min：写页面/脚本（必须能在浏览器打开）
- 20min：总结入 `W15-notes.md`
- 10min：口述复盘（解释：事件委托、ESM、Vue 的响应式直觉）

---

## Day 99（DOM 进阶：事件委托 + 动态列表）

学习要点
- 事件委托：把事件绑在父元素上，处理动态子元素
- `closest()`、`matches()`（简化 DOM 导航）

任务卡（70min）
- 在 `students.html`（或复制到新目录）实现：
  - 删除按钮：用事件委托处理（不要在每个按钮上单独绑）
  - 动态添加一行：输入临时数据后插入表格

验收标准
- 动态新增的行，同样能触发删除/操作（证明委托生效）

---

## Day 100（BOM：localStorage + 简单“状态持久化”）

学习要点
- `localStorage` 存 JSON：`JSON.stringify/parse`
- 版本字段：给存储数据加 `version`，避免以后结构升级难迁移

任务卡（70min）
- 封装 `storage.js`：
  - `loadStudents()` / `saveStudents(students)`
  - 数据不存在时返回默认示例数据

验收标准
- 刷新页面数据仍在；手动清空 localStorage 能恢复默认数据

---

## Day 101（ES6-1：解构、展开、默认参数）

学习要点
- 解构：对象/数组
- 展开：`...` 复制与合并
- 默认参数：减少 `undefined` 判断

任务卡（70min）
- 把 `studentsModel.js` 里的增删改查用 ES6 写法整理一遍（可读性优先）
- 写一个 `mergeStudent(base, patch)`：用展开合并字段

验收标准
- 你能解释：展开是“浅拷贝”，嵌套对象要注意什么（先建立概念）

---

## Day 102（ES6-2：模块化 ESM：import/export）

学习要点
- 浏览器原生 ESM：`<script type="module">`
- 拆分模块：model / view / storage

任务卡（70min）
- 把 JS 拆成模块：
  - `studentsModel.js` export CRUD
  - `studentsPage.js` 负责渲染与事件
- `index.html` 用 `type="module"` 引入入口 `app.js`

验收标准
- 模块边界清晰：model 不直接操作 DOM（或尽量少操作）

---

## Day 103（前端综合案例：仿站落地页 landing）

学习要点
- 页面分区：导航/主视觉/内容区/页脚
- 复用组件化思路：卡片、按钮、栅格（用 CSS 实现即可）

任务卡（70min）
- 新建 `web/w15/landing/index.html` + `styles.css`
- 至少包含：
  - 1 个主标题 + 副标题
  - 1 组课程卡片（6 张）
  - 1 个“立即学习”按钮（可以先 `href="#"`）

验收标准
- 手机宽度下也不“完全崩”（先做基础自适应：可用简单媒体查询）

---

## Day 104（Vue3 入门：CDN 版跑起来）

学习要点
- Vue 的核心：响应式数据驱动视图
- `createApp`、`data`、`methods`、`computed`

任务卡（70min）
- 新建 `web/w15/vue3-demo/index.html`
- 实现：
  - 列表渲染 `v-for`
  - 按钮 `v-on:click` 加分/减分
  - `computed` 计算平均分

验收标准
- 不依赖构建工具也能运行（双击打开或本地静态服务器均可）

---

## Day 105（周整合：把“学生管理”迁到 `web/w15/student-app`）

整合任务（必须）
- 完成一个可演示的“学生管理前端小应用”（仍可不连后端）
  - 列表：搜索/排序/删除
  - 表单：校验 + 写入 localStorage
  - 首页：展示统计信息（总人数/平均分）
- 代码结构要求：
  - 至少 3 个模块文件（storage/model/page）

验收标准（完成即过关）
- 页面交互完整；控制台无报错
- 你能用 90 秒讲清楚模块拆分理由
