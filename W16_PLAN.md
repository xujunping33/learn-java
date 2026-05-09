## 第16周学习计划：前端综合案例（仿站）+ Vue3 工程化入门

对应原路径：第15–16周《DOM/BOM/ES6/仿站/Vue3》中的 **后半段（综合案例 + Vue3工程化）**。  
学习时长：每天约 2 小时。

本周核心目标
- 完成一个更接近真实业务的“仿站首页”（静态资源即可，但结构要像项目）
- 用 **Vite + Vue3 + 单文件组件（SFC）** 搭一个可维护的小应用骨架
- 把你在 W14/W15 的“学生管理”迁移到 Vue 项目里（组件化 + 状态管理直觉）

本周交付物（必须完成）
- `web/w16/mukelike-home/`：仿站综合案例（至少包含：导航吸顶/轮播位/Banner、课程卡片网格、推荐区、页脚）
- `web/w16/vue-student-admin/`：Vite Vue3 项目（能 `npm install` + `npm run dev`）
  - 页面：`Home`（统计卡片）+ `Students`（表格 CRUD）+ `StudentForm`（表单校验）
  - 组件拆分：至少 `AppHeader`、`StudentTable`、`StatCards`
- `W16-notes.md`

目录建议
- `day106` ~ `day112`

每天固定节奏（2小时）
- 20min：复盘（昨天页面/项目在浏览器无报错）
- 70min：写页面或 Vue 代码（必须可运行）
- 20min：总结入 `W16-notes.md`
- 10min：口述复盘（解释：组件边界、props、状态提升）

---

## Day 106（仿站首页：信息架构 + 组件化切分）

学习要点
- 先把页面拆成“区块”，再拆成“可复用小块”（卡片/按钮/栅格）
- 目录组织：`index.html` + `styles/` + `assets/`

任务卡（70min）
- 新建 `web/w16/mukelike-home/`
- 完成页面信息架构（先不追求完美视觉）
  - Header（logo + nav + CTA）
  - Hero（标题 + 副标题 + 主按钮）
  - CourseGrid（6 张卡片）
  - Footer

验收标准
- 你能用 60 秒讲清楚：每个区块的职责是什么

---

## Day 107（仿站首页：布局与响应式基础）

学习要点
- CSS Grid / Flex 二选一（推荐 Flex + Grid 混用）
- 媒体查询：至少 1 个断点（例如 768px）

任务卡（70min）
- 让 `CourseGrid` 在宽屏 3 列、窄屏 1 列
- Header 在窄屏变成“可换行/可压缩”的布局（先不强制做汉堡菜单）

验收标准
- 窗口缩放时布局不崩（允许不完美，但不能重叠严重）

---

## Day 108（仿站首页：细节打磨与可维护 CSS）

学习要点
- CSS 变量：`--color-primary`（统一主题色）
- BEM 或前缀命名（二选一，保持一致）

任务卡（70min）
- 抽离 `tokens.css`（颜色/圆角/阴影/间距）
- 统一按钮/卡片样式（减少重复 CSS）

验收标准
- 改一个主题色变量，全站主色能跟着变（至少覆盖按钮与链接）

---

## Day 109（Vite + Vue3：创建工程 + 跑通 HelloWorld）

学习要点
- Node/npm 基础：`package.json`、脚本命令
- Vite 的定位：开发服务器 + 打包

任务卡（70min）
- 创建 `web/w16/vue-student-admin/`（Vite Vue 模板）
- 跑通：
  - `npm install`
  - `npm run dev`
- 改 `App.vue`：显示你的项目标题 + 当前时间（用 `computed` 或 `setInterval` 二选一）

验收标准
- 你能解释：`npm run dev` 和 `npm run build` 分别做什么

---

## Day 110（Vue3 SFC：组件拆分 + props + emits）

学习要点
- 单文件组件：`<template><script setup>`
- 父传子：`props`
- 子通知父：`emit`

任务卡（70min）
- 拆分组件
  - `StatCards.vue`：接收 `students` 计算总人数/平均分
  - `StudentTable.vue`：展示列表 + 删除事件 `emit('delete', id)`
- `Students.vue`：作为页面容器组合组件

验收标准
- 子组件不直接改 props（需要改数据时通过 emit 交给父组件）

---

## Day 111（Vue3：表单组件 + 简单校验 + 列表增删）

学习要点
- `v-model` 双向绑定（表单）
- 列表更新：不可变更新直觉（替换数组/拷贝对象）

任务卡（70min）
- `StudentForm.vue`：新增学生（name/score/age）
  - 校验：name 非空、score 0–100、age 合理范围
- `Students.vue`：新增后刷新列表（或直接把新学生 push 到状态）

验收标准
- 非法输入能阻止提交，并给出明确提示

---

## Day 112（周整合：仿站 + Vue 学生管理合成一个“可演示作品集入口”）

整合任务（必须）
- 新建 `web/w16/README.md`（很短即可）
  - 写清楚如何启动 Vue 项目
  - 写清楚如何打开仿站首页（静态打开或 `python -m http.server`）
- 把 Vue 项目里页面导航串起来（可用简单顶部导航）

验收标准（完成即过关）
- 你能独立演示：仿站首页 + Vue 学生管理
- 你能口述：为什么 Vue 项目比“纯 HTML+JS”更适合中大型页面
