## 第14周学习计划：JavaScript（DOM/BOM + 基础语法 + 小交互）

对应原路径：第13–14周《HTML/CSS/JS》中的 **JavaScript 部分**。  
学习时长：每天约 2 小时。

本周核心目标
- 掌握 JS 基础语法：变量、运算符、分支、循环、函数、数组
- 会用 DOM 获取/修改页面元素（列表渲染、表单校验、按钮事件）
- 会用 BOM 的基础能力（`window` 事件、定时器）
- 把你 `web/w13/` 的静态页面升级为“可交互页面”（仍然纯前端）

本周交付物（必须完成）
- 在 `web/w13/` 增加 `js/` 目录，并拆分脚本（避免所有逻辑堆在 HTML 里）
  - `js/app.js`：通用初始化（导航高亮等）
  - `js/students.js`：学生列表交互（搜索/删除行/动态渲染）
  - `js/student-form.js`：表单校验与提交提示（先不连后端）
- `W14-notes.md`

目录建议
- `day92` ~ `day98`（练习草稿可放 `web/w13/drafts/`）

每天固定节奏（2小时）
- 20min：复盘（昨天页面在浏览器控制台无报错）
- 70min：写 JS（必须能在浏览器看到效果）
- 20min：总结入 `W14-notes.md`
- 10min：口述复盘（解释：事件冒泡、DOM 查询、数组处理）

---

## Day 92（JS入门：变量、运算符、类型直觉）

学习要点
- `let/const`（优先 const，需要可变再用 let）
- 基本类型与 `typeof`
- 模板字符串：`` `hello ${name}` ``

任务卡（70min）
- 新建 `web/w13/js/utils.js`（可选）写几个小函数
  - `clampScore(n)`：把分数限制在 0–100
  - `isNonEmptyString(s)`：判断是否非空字符串
- 在 `student-form.html` 引入脚本（先只做 console 输出也行）

验收标准
- 你能解释：`==` 与 `===` 的差异（本周开始尽量用 `===`）

---

## Day 93（控制流：if/switch/for/while）

学习要点
- 分支与循环写清楚边界（和后端一样重要）
- `for...of` 遍历数组（现代写法）

任务卡（70min）
- 写一个 `filterStudents(students, keyword)`：按 name 包含关键字过滤
- 写一个 `statsScores(scores)`：返回 `{min,max,avg}`

验收标准
- 空数组/空关键字等边界不会报错（返回合理结果）

---

## Day 94（函数：声明函数 vs 箭头函数）

学习要点
- 函数是一等公民：可以当参数传递
- 箭头函数：适合短回调（排序、过滤）

任务卡（70min）
- 用箭头函数改写昨天的过滤/统计函数
- 给 `students.html` 增加一个“搜索框 + 按钮”，点击后过滤表格行（先不改数据结构也行）

验收标准
- 你能解释：什么时候箭头函数更不适合（例如需要 `this` 的场景，先建立直觉）

---

## Day 95（数组：map/filter/sort/reduce 基础）

学习要点
- 数据处理管道：filter → map → sort
- `reduce`：求和/计数（先会用即可）

任务卡（70min）
- 把表格数据改成 JS 数组对象：`{id,name,score,age}`
- 实现：
  - 按分数排序（升序/降序切换）
  - 统计平均分（展示在表格上方）

验收标准
- 排序/统计按钮触发后页面即时更新

---

## Day 96（DOM：查询、修改文本、增删节点）

学习要点
- `document.querySelector` / `querySelectorAll`
- 修改 `textContent`、样式（classList）
- 动态创建 `tr/td` 并插入表格

任务卡（70min）
- 把 `students.html` 的行渲染改为 JS 渲染（数据驱动）
- 实现删除一行（从数组删除 + 重新渲染 或 直接删 DOM）

验收标准
- 页面结构不再依赖“写死的很多行 tr”，而是能由数据生成

---

## Day 97（事件：click/input/submit + 简单校验）

学习要点
- `addEventListener`
- 表单 `submit` 默认行为：`preventDefault()`
- 简单校验：空值、分数范围、年龄范围

任务卡（70min）
- `student-form.js`：提交时校验，不通过则提示并阻止提交
- 校验规则写清楚（函数拆分：`validateStudentForm()`）

验收标准
- 非法输入不会“静默失败”，用户能看到明确提示

---

## Day 98（BOM：window/load/定时器 + 周整合）

学习要点
- `window.onload` 或 `DOMContentLoaded`（二选一，理解差异）
- `setTimeout` / `setInterval`（用于简单提示或轮询）

任务卡（70min）
- 周整合：把列表页 + 表单页 + 首页串起来
  - 首页展示统计卡片（总人数/平均分）
  - 列表页支持搜索、排序、删除
  - 表单页校验完善
- 用 `localStorage` 保存学生列表（可选，但非常推荐）

验收标准（完成即过关）
- 打开页面有交互；控制台无报错
- 刷新页面后数据是否还在：如果你做了 localStorage，应能保留（并在笔记里写清楚）
