# 第 16 周笔记（仿站 + Vue3 工程化）

## Day 106：仿站信息架构（mukelike-home）

- 路径：**`web/w16/mukelike-home/`**：**`index.html`** + **`styles/main.css`** + **`assets/`**（占位，后续放图/字体等）。
- **区块职责（口述 60s 用）**：
  - **Header**：品牌识别、全站主导航、**主转化 CTA**（注册/登录类）；**`sticky`** 便于长页随时跳转。
  - **Hero**：一句话价值 + 副文案 + **主按钮 / 次按钮**，承接推广落地。
  - **CourseGrid**：课程 SKU 栅格；单张 **`.course-card`** 是可复用「小块」模板。
  - **Footer**：版权、协议、联系方式等次要信息，与主转化区分。
- **扩展**：**`#paths` 占位条** 提示后续可加路径/讲师/推荐区，避免首版塞满导致结构混乱。

## Day 107：响应式（mukelike-home）

- **断点**：**`@media (max-width: 768px)`**（变量 **`--bp-md`** 与注释对齐）。
- **CourseGrid**：默认 **`repeat(3, minmax(0, 1fr))`** 防溢出；窄屏 **`grid-template-columns: 1fr`** 单列；卡片 **`min-width: 0`**。
- **Header**：窄屏 **`flex-direction: column`**，Logo 居中、导航 **`justify-content: center`** 可换行、CTA **全宽上限 + 居中**（未做汉堡菜单）。
- **Hero / Footer**：略减内边距；页脚链接区窄屏纵向居中，避免与版权挤在同一行重叠。

## Day 108：tokens + 可维护 CSS

- **`styles/tokens.css`**：颜色（**`--color-primary`**、**`--color-link`** 等）、**`color-mix`** 派生 hover、圆角、间距、阴影、断点；**`index.html`** 先 **`tokens.css`** 再 **`main.css`**。
- **`main.css`**：布局与 BEM 块样式，尺寸优先 **`var(--space-*)`**。
- **改一处主色**：只改 **`tokens.css`** 里的 **`--color-primary`**，主按钮、描边按钮、Hero 强调与光晕、卡片 hover 边、**正文链接**会一起变（链接由 **`color-mix`** 基于主色生成）。

## Day 109：Vite + Vue3 工程跑通

- 路径：**`web/w16/vue-student-admin/`**（**`npm create vite@latest … --template vue`**）。
- 命令：**`npm install`** → **`npm run dev`**（开发热更新）；**`npm run build`** → 产出 **`dist/`** 静态资源；**`npm run preview`** 本地预览构建结果。
- **`App.vue`**：项目标题 + **`setInterval`** 每秒刷新 **`currentTime`**（**`onUnmounted`** 里 **`clearInterval`**）。
- **口述**：**`npm run dev`** 起 Vite 开发服务器 + HMR；**`npm run build`** 做生产打包（压缩、tree-shaking、资源哈希等），部署通常用 **`dist`**。

## Day 110：SFC 拆分 + props + emit

- **`StatCards.vue`**：**`defineProps({ students })`**，用 **`computed`** 算人数、平均分（空列表平均分为 **`—`**）。
- **`StudentTable.vue`**：只展示列表；**`defineEmits(['delete'])`**，删除按钮 **`emit('delete', id)`**，**不直接改 props**。
- **`Students.vue`**：父级 **`ref`** 持有列表，**`removeById`** 用 **`filter`** 做不可变更新；组合 **`<StatCards />`** + **`<StudentTable @delete="removeById" />`**。
- **`App.vue`**：页头标题 + 时钟 + **`<Students />`**。
- **口述**：子组件通过 **emit** 把意图交给父组件，由父组件改状态，避免子组件改 props 破坏单向数据流。

## Day 111：表单 + 校验 + 列表新增

- **`StudentForm.vue`**：**`v-model`** / **`v-model.trim`** 绑定姓名、分数、年龄；**`novalidate`** 后用脚本校验（避免仅靠浏览器默认提示）；非法则 **`role="alert"`** 字段错误文案，**不 emit**。
- **规则**：姓名非空；分数 **0～100** 数字；年龄 **6～99** 整数。
- **`Students.vue`**：**`addStudent`** 用 **`[...students, row]`** 追加；**`nextStudentId`** 取当前 **`max(id)+1`**。
- **口述**：列表更新优先拷贝/替换引用（**不可变**），便于 Vue 追踪依赖与后续接持久化。

## Day 112：周整合（作品集入口）

- **`web/w16/README.md`**：仿站静态打开 / **`python -m http.server`**；Vue 项目 **`npm install` · `npm run dev` · `build` / `preview`**；附口述要点（组件化 + 工程化 vs 纯 HTML/JS）。
- **Vue 导航**：**`AppHeader.vue`**（首页 / 学生管理）；**`activePage`** + **`v-if`** 切换 **`HomeView`**（**`StatCards`**）与 **`Students`**（表单 + 表格）。
- **状态提升**：**`students` / `addStudent` / `removeById`** 集中在 **`App.vue`**，两页共享同一数据源。
