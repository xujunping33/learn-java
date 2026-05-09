# 第 13 周笔记（HTML + CSS 静态页）

## 周项目交付

- 目录：`web/w13/`
- 入口：浏览器打开 `web/w13/index.html`（或本地静态服务器根目录指向 `web/w13`）。
- 页面：`index.html` → `pages/students.html` → `pages/student-form.html`，侧栏导航互通。

## 自检清单（Day 91）

- [ ] 三页侧栏链接可往返首页、列表、表单。
- [ ] 表格有 `caption`、`thead`/`tbody`、`th` 的 `scope`，窄屏时表格外层可横向滚动（`.table-wrap`）。
- [ ] 表单含姓名、成绩、年龄，`label` 与 `id` 对应，提交与重置可用。

## 口述：哪些是「语义化」结构，为什么重要

- **`header` / `nav` / `main` / `section` / `footer`**：表达页面区域角色，读屏与 SEO 更易理解，样式也可按区域挂接。
- **`h1`–`h2` 层级**：标题大纲清晰，辅助技术可按标题导航。
- **表格用 `th` + `scope`**：标明表头与数据关系，不仅靠视觉对齐。
- **表单 `label for` + 控件 `id`**：点击标签即可聚焦输入，可访问性更好。

## 术语速记

- **`padding` 与 `margin`**：`padding` 在边框内侧；`margin` 在边框外侧，控制与相邻元素的间距。
- **优先用 `class`**：可复用、易覆盖；`id` 唯一且特异性高，适合锚点或脚本单点而不是满屏样式。
- **侧栏布局**：左栏 `float` + 主栏 `overflow: hidden`（BFC）+ 父级 **clearfix**，避免主内容与浮动重叠。
