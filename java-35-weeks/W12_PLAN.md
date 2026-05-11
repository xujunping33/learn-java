## 第12周学习计划：MyBatis（从JDBC升级到ORM）

对应原路径：第12周《Java必知必会框架入门-MyBatis》。  
学习时长：每天约 2 小时。

本周核心目标
- 会跑通 MyBatis 的基本开发流程（配置 → SqlSessionFactory → Mapper → CRUD）
- 熟练参数传递（单参/多参/对象/Map）
- 能做多表关联查询（employee ↔ department）
- 会写动态 SQL（if/where/trim/choose/foreach）
- 理解一级缓存与二级缓存（先会观察现象，再理解原理）
- 使用 logback 输出 SQL 日志，定位问题更快

本周交付物（必须完成）
- `mybatis-demo/`：一个 Maven MyBatis 项目（能运行 main 或单测）
- `StudentMapper`：student 表 CRUD + 条件查询 + 分页
- `EmployeeMapper`：employee 表 CRUD + 关联 department 查询
- `DynamicSqlDemo`：至少 3 个动态 SQL 场景（条件筛选/批量插入/IN 查询）
- `W12-notes.md`

目录建议
- `day78` ~ `day84`
- 建议新建 `mybatis-demo/`（从本周开始，你的“后端主线项目”会逐渐成型）

每天固定节奏（2小时）
- 20min：复盘（重跑昨日 demo，修 1 个配置/SQL 问题）
- 70min：主编码任务（必须可运行/可测试）
- 20min：总结入 `W12-notes.md`
- 10min：口述复盘（说清：MyBatis 相比 JDBC 帮你解决了什么）

---

## Day 78（MyBatis 基本流程：跑通第一条 SQL）

学习要点
- 核心对象：`SqlSessionFactory`、`SqlSession`、`Mapper`
- 配置方式：XML（本周先用 XML，理解原理更清晰）
- Mapper 与 SQL 的绑定（namespace + id）

任务卡（70min）
- 创建 `mybatis-demo` Maven 项目（依赖：mybatis、mysql-connector-j、slf4j/logback）
- 配置 `mybatis-config.xml`（数据源、mapper）
- 写 `StudentMapper.selectById` 并跑通

验收标准
- 你能稳定跑通：输入 id → 查询 student → 映射成对象

---

## Day 79（参数传递：单参/多参/对象/Map）

学习要点
- 单参数：`#{id}`
- 多参数：`@Param("x")` 或 `param1/param2`
- 对象参数：`#{name}`、`#{score}`
- Map 参数：按 key 取值

任务卡（70min）
- 给 `StudentMapper` 增加方法
  - `selectByName(String name)`
  - `selectByScoreRange(int min, int max)`（多参 + @Param）
  - `insert(Student s)`（对象传参）
- 写一个 Map 参数的查询（可选）

验收标准
- 你能解释：为什么多参建议用 `@Param`

---

## Day 80（多表关联查询：employee ↔ department）

学习要点
- 关联关系：employee.dept_id → department.id（你 W9 已建好）
- 映射：`resultMap` + `association`
- 一对多（先了解）：`collection`

任务卡（70min）
- 写 `Department`、`Employee` 实体（包含关联字段）
- 写 `EmployeeMapper.selectWithDeptById(long id)`
  - 返回 Employee，同时包含 Department 对象
- 写 `DepartmentMapper.selectById`

验收标准
- 你能跑通：查询 employee 时把部门一起查出来（对象嵌套）

---

## Day 81（动态 SQL：让查询“可选条件”变简单）

学习要点
- `<if>`、`<where>`、`<trim>`
- `<choose>/<when>/<otherwise>`
- 避免手写拼接 SQL（安全 + 可维护）

任务卡（70min）
- 写 `StudentMapper.selectByCondition(StudentQuery q)`
  - 条件可选：name 模糊、scoreMin/scoreMax、age
  - 用 `<where>` 自动处理 AND
- 写 `EmployeeMapper.selectByDeptIds(List<Long> deptIds)`（为 Day82 foreach 做铺垫）

验收标准
- 你能解释：动态 SQL 如何避免多余的 AND/WHERE

---

## Day 82（foreach：批量插入/IN 查询/批量删除）

学习要点
- `<foreach>`：collection/item/index/open/close/separator
- 常见场景：IN、批量 insert、批量 delete

任务卡（70min）
- 批量插入学生：`insertBatch(List<Student> list)`
- IN 查询：`selectByIds(List<Long> ids)`
- 批量删除：`deleteByIds(List<Long> ids)`（可选）

验收标准
- 你能跑通：传入 5 条数据一次性插入成功

---

## Day 83（分页：LIMIT + 参数封装（PageHelper先放到后面））

学习要点
- MySQL 分页：`LIMIT offset, size`
- offset 计算：`(page-1)*pageSize`
- 分页要配稳定排序（通常 id）

任务卡（70min）
- 写 `StudentMapper.selectPage(int offset, int pageSize)`
- 封装一个 `PageRequest(page,pageSize)`（可选）

验收标准
- 你能解释：为什么分页必须配 ORDER BY，否则数据可能“跳动”

---

## Day 84（缓存 + 日志：看懂 MyBatis 在干什么）

学习要点
- 一级缓存：SqlSession 级别（同一 session 内重复查询的现象）
- 二级缓存：Mapper 级别（先理解“跨 session 缓存”，再决定是否开启）
- logback：SQL 打印与参数定位

任务卡（70min）
- 配 logback 输出 MyBatis SQL（能看到 SQL + 参数）
- 写 `CacheDemo`
  - 同一 SqlSession 内同一查询跑两次，观察是否命中一级缓存
  - 不同 SqlSession 再查一次，观察差异
- （可选）开启二级缓存并观察

验收标准（完成即过关）
- 你能口述：一级缓存为什么默认安全；二级缓存为什么需要谨慎

