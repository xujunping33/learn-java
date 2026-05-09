# 第 14 周笔记（JavaScript）

## Day 92：`==` 与 `===`

- **`===`**：严格相等，**不**做类型转换；类型不同直接为 `false`。本周默认用它。
- **`==`**：会做隐式类型转换再比（例如 `"0" == 0` 为 `true`），容易踩坑，除非刻意需要宽松比较，否则少用。

Day 92 交付：`web/w13/js/utils.js`（`clampScore`、`isNonEmptyString`）、`student-form.html` 引入 `utils.js` + `student-form.js`，打开表单页在控制台可见探针输出。

## Day 93：控制流与边界

- **`filterStudents(students, keyword)`**：`students` 非数组 → `[]`；关键字去首尾空格后为空 → 返回原列表副本（`slice`）；否则按 `name` 不区分大小写包含过滤。
- **`statsScores(scores)`**：非数组或空数组 → `{ min: null, max: null, avg: null }`；仅统计可转为有效数字的项；`min` / `max` / `avg` 均基于有效数字子集。
- 遍历有效分数使用 **`for...of`**。

打开 `pages/students.html`，控制台可见 `[Day93]` 探针输出。

## Day 94：箭头函数与搜索

- **`filterStudents` / `statsScores`** 改为 **`const xxx = (...) =>`**；统计里用 **`map` / `filter` / `reduce`** 与 **`Math.min` / `Math.max`**（回调多为箭头函数）。
- **列表页**：`#student-search` + `#student-search-btn`，点击「搜索」用 `filterStudents(STUDENTS, keyword)` 得到 id 集合，给 `#student-table tbody tr[data-student-id]` 设 **`hidden`**。

**箭头函数何时不合适**：需要依赖调用方提供的 **`this`**（如 DOM 回调里要用元素自身、且依赖传统函数的动态 `this`）时，不宜盲目改成箭头函数；对象方法若要用 `this` 指向实例，常写普通方法或显式绑定。

## Day 96：DOM 渲染与删除

- **`tbody#student-tbody`** 初始为空；**`renderTable`** 用 **`createElement("tr" / "td")`**、**`textContent`** 填单元格，**`appendChild`** 挂载；数据为 **`{ id, name, score, age }[]`**（`let students` 可变）。
- **删除**：**`tbody`** 上 **`click`** 事件委托，匹配 **`button[data-action='delete']`** → **`splice` + `renderTable` + `applySearch`**（搜索条件仍生效）。
- 查询：**`getElementById` / `querySelectorAll`**；行显示：**`tr.hidden`**（与搜索联动）。

## Day 97：表单提交与校验

- 表单 **`novalidate`**：关闭浏览器原生校验，由脚本统一提示（避免与自定义文案冲突）。
- **`submit`**：**`preventDefault()`** → **`validateStudentForm()`**；失败则 **`#form-error`**（**`role="alert"`**）显示文案；成功用 **`alert`** 提示演示通过（未接后端）。
- 规则：**`isNonEmptyString`** 校验姓名；成绩 **0～100**、年龄 **1～120**、**`Number.isNaN`** 拦截非数字。
- **`reset`** 与 **`input`（捕获）** 时清除错误提示，避免旧错误残留。

## Day 98：BOM、localStorage、周整合

- **存储**：`js/storage.js`，键名 **`learn-java-w13-students-v1`**。`loadStudents` / `saveStudents` / `nextStudentId`。无数据时返回内置 5 条默认副本；写入后刷新页面仍从 **`localStorage`** 读取。
- **首页**：`js/app.js` 在 **`DOMContentLoaded`** 后用 **`setTimeout(..., 0)`** 更新 **`#stat-count`**、**`#stat-avg`**（`statsScores`）。
- **列表**：`students.js` 读存储、删后保存、**升序/降序** 排序后保存；搜索与 Day 94 相同。
- **表单**：校验通过后 **`push` + `saveStudents`**，**`setTimeout`** 内 **`alert`** 并跳转 **`students.html`**。
- **清空数据**：在控制台执行 **`localStorage.removeItem('learn-java-w13-students-v1')`** 后刷新，可恢复默认 5 条（下次加载仍来自默认副本直至再次保存）。

**`DOMContentLoaded` 与 `window.onload`**：前者在 HTML 解析完、DOM 可查询时触发；后者等图片等资源加载更晚。本页脚本用 `DOMContentLoaded` 即可。
