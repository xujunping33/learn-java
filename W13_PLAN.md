## 第13周学习计划：HTML + CSS（静态页面与布局）

对应原路径：第13–14周《HTML/CSS/JS》中的 **HTML/CSS 部分**。  
学习时长：每天约 2 小时。

本周核心目标
- 能写语义化 HTML（标题、段落、列表、图片、链接）
- 会用表格与表单（后端开发最常打交道）
- 会用 CSS 做基础样式与页面布局（选择器、盒模型、定位/浮动）
- 能做出一个“像样”的静态页面（为第14周 JS 交互做准备）

本周交付物（必须完成）
- `web/w13/` 目录（建议新建）
  - `index.html`：页面骨架 + 导航 + 内容区
  - `styles/base.css`：全局样式（字体、颜色、间距）
  - `styles/layout.css`：布局（头部/侧边栏/主内容/页脚）
  - `pages/students.html`：学生列表（表格）
  - `pages/student-form.html`：新增/编辑学生（表单）
- `W13-notes.md`

目录建议
- `day85` ~ `day91`（你可以把练习草稿也放 `web/w13/drafts/`）

每天固定节奏（2小时）
- 20min：复盘（昨天页面在浏览器里再打开一遍，修 1 个结构/样式问题）
- 70min：写 HTML/CSS（必须能在浏览器打开看到效果）
- 20min：总结入 `W13-notes.md`
- 10min：口述复盘（解释：语义化标签、盒模型、定位差异）

---

## Day 85（HTML结构：语义化与常用标签）

学习要点
- `html/head/body`、`meta charset`
- 标题 `h1-h6`、段落 `p`、列表 `ul/ol/li`
- 链接 `a`、图片 `img`（alt 很重要）

任务卡（70min）
- 新建 `web/w13/index.html`
- 写：站点标题、简介、导航链接到两个页面（列表页/表单页）

验收标准
- 页面结构清晰：一眼能看出“导航/内容/页脚”

---

## Day 86（表格：table/thead/tbody/tr/th/td）

学习要点
- 表格用于结构化数据展示（列表页最常见）
- `caption`（可选）、`th` 与 `td` 的区别

任务卡（70min）
- 新建 `web/w13/pages/students.html`
- 做一个学生列表表格（至少 5 行假数据）
  - 列：id、name、score、age、操作（先放占位按钮）

验收标准
- 表头与数据对齐合理；缩进与可读性良好

---

## Day 87（表单：input/select/textarea/button）

学习要点
- `form` 的 `action/method`（先理解概念，后续联调会用到）
- 常用控件：`text/number/email/password/date`
- `label` 与 `for` 绑定（可访问性 + 更好点选体验）

任务卡（70min）
- 新建 `web/w13/pages/student-form.html`
- 字段：name、score、age（先用 number）
- 增加提交按钮与重置按钮

验收标准
- 表单字段都有 label；输入类型基本合理

---

## Day 88（CSS入门：颜色/字体/间距/盒模型）

学习要点
- 选择器：元素/类/ID
- 盒模型：`margin/padding/border`、`box-sizing`
- 常用单位：`px`、`rem`（了解即可）

任务卡（70min）
- 新建 `web/w13/styles/base.css`
- 给 `index.html` 与两个页面引入 CSS
- 统一：字体、背景、主色、按钮样式、表格样式

验收标准
- 你能解释：`padding` 和 `margin` 的区别

---

## Day 89（CSS选择器：组合选择器与优先级）

学习要点
- 组合选择器：后代、子代、相邻兄弟（了解）
- 优先级直觉：inline > id > class > 元素

任务卡（70min）
- 给表格行加斑马纹（`tr:nth-child`）
- 给导航当前页高亮（用 class，例如 `active`）

验收标准
- 你能解释：为什么尽量用 class，而不是到处写 id

---

## Day 90（布局：浮动/定位/简单两栏布局）

学习要点
- `float`：经典布局手段（了解即可，很多老页面会遇到）
- `position: relative/absolute`：覆盖与定位
- 实用技巧：清除浮动（了解即可）

任务卡（70min）
- 新建 `web/w13/styles/layout.css`
- 做一个简单布局
  - 顶部导航固定高度
  - 左侧窄栏（放菜单链接）
  - 右侧主内容（放表格或表单）

验收标准
- 页面在浏览器窗口缩放时仍“基本不乱”（先不追求完美响应式）

---

## Day 91（周项目：静态“学生管理系统”页面）

整合任务（必须）
- 把 `index.html / students.html / student-form.html` 串成一个完整静态站点
- 统一导航、统一风格、统一页脚
- 自检清单
  - 三个页面互相跳转正常
  - 表格可读性良好
  - 表单字段完整

验收标准（完成即过关）
- 你能用 60 秒演示：列表页 + 表单页 + 返回首页
- 你能口述：哪些结构是“语义化”的，为什么重要
