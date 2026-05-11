# 第 15 周笔记（DOM/BOM + ES6 + 综合案例 + Vue3）

## Day 99：事件委托 + 动态列表

- **事件委托**：在 **`#student-tbody`** 上监听 **`click`**，用 **`event.target.closest("button[data-action='delete']")`** 找到实际点击的删除按钮；子节点是后来 **`renderTable`** 插入的也没关系，无需给每个按钮 **`addEventListener`**。
- **`closest` / `matches`**：`closest` 从点击目标向上找匹配选择器的祖先；确认节点类型后可再 **`matches("button[data-action='delete']")`**，避免误点到嵌套结构。
- **动态添加**：「快速添加一行」经数据层 **`addStudent`** 后 **`saveStudents`**，再 **`renderTable`**（Day 101 起）；新行上的删除按钮与旧行共用同一套委托，用于验收「新增行也能删」。

## Day 100：localStorage 与 version

- 存盘格式：**`{ "version": 1, "students": [ ... ] }`**，与旧版「纯数组」JSON **兼容**：首次读取旧数据后会 **`saveStudents`** 写回信封格式，完成迁移。
- **`loadStudents()`**：无键或解析失败 → 返回内置默认 5 人；有键且为合法信封或旧数组 → 返回 **`students`** 数组（可为空数组，表示用户已删光）。
- **`saveStudents(list)`**：始终 **`JSON.stringify`** 带 **`version`** 的对象。
- **手动清空**：开发者工具 Application → Local Storage 删掉对应键，或控制台 **`localStorage.removeItem('learn-java-w13-students-v1')`**，刷新后无键 → 再走默认数据逻辑。

## Day 101：ES6 与 `mergeStudent`

- **`mergeStudent(base, patch = {})`**：**`{ ...base, ...patch }`** 浅合并；**`patch`** 里同名字段覆盖 **`base`**。
- **浅拷贝**：第一层键是新对象；若 **`base.meta`** 是对象，展开后 **`meta`** 仍指向**同一引用**，改 **`patch.meta`** 与 **`base.meta`** 可能互相影响——嵌套结构要再展开或深拷贝工具。
- **数据层**：逻辑在 **`js/esm/studentsModel.js`**（Day 102 起为 ESM；此前非 module 版在 **`js/studentsModel.js`** 可作对照）。

## Day 102：原生 ESM

- **`<script type="module">`**：模块默认 **defer**，执行顺序按文档顺序；**`import`/`export`** 相对路径以**当前模块文件**为基准（如 `pages/students.html` 引用 `../js/esm/studentsApp.js`，其内部 `./studentsPage.js` 解析到同目录）。
- **拆分**：`js/esm/storage.js`（持久化）、`utils.js`（纯函数）、`studentsModel.js`（**不访问 DOM**，只依赖 `storage`）、`studentsPage.js`（列表 **DOM + 事件**）、入口 **`studentsApp.js`**；首页统计入口按计划为 **`js/esm/app.js`**；表单为 **`formApp.js`**。
- **旧脚本**：`web/w13/js/*.js`（非 module）已不再被 HTML 引用，可归档删除或保留作对照。

## Day 103：仿站落地页 landing

- 路径：**`web/w15/landing/index.html`** + **`styles.css`**。
- 结构：**顶栏导航**（`sticky`）→ **Banner**（主标题 + 副标题 + **「立即学习」** `href="#"`）→ **课程卡片栅格**（6 张 **`article.card`**）→ **页脚**。
- 响应式：**`@media (max-width: 900px)`** 两列、**`520px`** 以下导航纵向 + 卡片单列；标题用 **`clamp`** 缩放。

## Day 104：Vue3 CDN

- 路径：**`web/w15/vue3-demo/index.html`**，通过 **`https://unpkg.com/vue@3/dist/vue.global.prod.js`** 引入 **`Vue`**，使用 **`createApp({ data, methods, computed }).mount('#app')`**（选项式 API）。
- **`v-for`**：列表渲染，**`:key="item.id"`**；**`@click`**：调用 **`changeScore`** 做 ±1 并限制在 0～100。
- **`computed.avgScore`**：由 **`students`** 派生，分数变化时视图自动更新（响应式）。
- 完全无构建步骤；若 **`file://`** 下 CDN 被策略拦截，换本地静态服务或下载 **`vue.global.prod.js`** 改相对路径引用。

## Day 105：student-app 周整合

- 路径：**`web/w15/student-app/`**：**`index.html`**、**`pages/students.html`**、**`pages/student-form.html`**。
- **模块**：**`js/modules/storage.js`**、**`utils.js`**、**`studentsModel.js`**；**`js/pages/studentsPage.js`**、**`homePage.js`**、**`studentFormPage.js`**；入口 **`js/app.js`**、**`js/studentsApp.js`**、**`js/formApp.js`**。
- **localStorage**：仍用键 **`learn-java-w13-students-v1`**，与 **`web/w13`** 互通。
- **样式**：复用 **`../../w13/styles/`**（根 **`index.html`** 为两级上到 **`web`**；**`pages/*`** 为 **`../../../w13/styles/`**）。
- **口述拆分**：storage 只管持久化；model 只做不可变数据变换；page 管 DOM 与事件并调用前两者；utils 放与 UI 无关的纯函数。
