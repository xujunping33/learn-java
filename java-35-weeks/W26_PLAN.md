# 第 26 周学习计划：Spring Boot 3 深化（MyBatis-Plus / 接口文档 / 可观测 / 测试与配置）

> 对应总纲：**第25–26周（Spring Boot 3 + JDK21 新特性 + MyBatis/MyBatis Plus）** 中的 **第26周（在 W25 已跑通主流程的基础上加深）**。  
> 本周定位：在 **`boot-social-demo`** 上迭代，把 **MyBatis-Plus**、**对外协作（OpenAPI）**、**运行时可观测（Actuator）**、**配置与测试** 做到「能交出去给别人联调/部署」的程度。  
> 时间：约 **2 小时/天**；本周 **Day 176 ~ Day 182**。  
> **注意**：**Redis / Nginx / 完整 Docker 上线链路** 按总纲放在 **W27–W28**；本周只做 **轻量容器化预习**（可选）或把精力放在 MP 与工程化。

---

## 本周总目标

- **MyBatis-Plus**：至少把 **1～2 个核心表** 从手写 XML/注解迁移到 **BaseMapper**，并用上 **分页** 与 **`LambdaQueryWrapper`/`UpdateWrapper`** 的典型写法。
- **接口契约**：接上 **springdoc-openapi**，能在浏览器打开 **Swagger UI**，请求/响应/错误模型可读。
- **可观测**：引入 **Spring Boot Actuator**，理解 **health/info**、生产环境 **端点暴露** 原则（不落敏感信息）。
- **配置与安全习惯**： **`@ConfigurationProperties`** 绑定业务配置（如分页默认页大小、允许的 CORS）；敏感信息不进仓库（`.env`/环境变量/本地 `application-local.yml`）。
- **测试**：至少 **一条** 「连真实 MySQL 语义」的集成测试路线：**Testcontainers MySQL（推荐）** 或 **`@SpringBootTest` + docker-compose MySQL（文档化）** 二选一，跑通_register→login→发帖_主链路。
- **JDK 21**：在项目中 **有意识地用** 一两个新语法点（不要求贪多）：例如 **record DTO**、**switch 表达式**、`instanceof` 模式匹配、`Text Blocks`（多行 SQL/字符串）。

---

## 工程约束（接上 W25）

- 继续在 **`boot-social-demo/`** 上做增量；避免另起炉灶。
- 保持 **W25 已定的分层**：`web` / `service` / `mapper` / `model` / `config`。
- 每完成一天：**`mvn -q test` 全绿** 再进入下一天（习惯比进度更重要）。

---

## Day 176（周一）— 接入 MyBatis-Plus：依赖 + 配置 + 第一张表「MP 化」

**学什么**

- MP 与原生 MyBatis 的关系：`BaseMapper<T>`、通用 CRUD、`@Mapper` 扫描、`mybatis-plus-spring-boot3-starter`（Boot 3 用 **boot3** 坐标）。
- 全局配置：`id-type`、下划线映射、分页插件 **`PaginationInnerInterceptor`**（先配好，明天用分页）。

**做什么**

1. 在 `pom.xml` 增加 MP starter（对齐 Spring Boot 3 / Jakarta）。
2. 增加 `MybatisPlusConfiguration`：**分页插件**、（可选）`MybatisPlusInterceptor`。
3. 选一个实体：**`User` 优先**（或 `Post`），新增 `extends BaseMapper<X>` 的 Mapper，并让 Service 的一条路径改用 MP（例如按用户名查询 `LambdaQueryWrapper`）。
4. 确认 **与原 MyBatis 共存**（若暂时双栈：新旧 mapper 分清包名或命名，避免重复扫描冲突）。

**验收**

- 应用启动正常；沿用原接口行为不变（注册/登录仍可用）。
- README 增补一行：**MP 版本与分页插件已启用**。

---

## Day 177（周二）— 分页与条件查询：把「帖子列表」做成标准形态

**学什么**

- **`Page<X>` / `IPage<X>`**：`current`、`size`、`total`、排序字段。
- `LambdaQueryWrapper`：动态 `like`、`eq`、`orderByDesc`，避免字符串拼字段名。

**做什么**

1. 将 `GET /api/posts` 改为使用 MP 分页（若你 W25 已手写 limit/offset，本周 **替换为 MP 分页**）。
2. 增加可选筛选（至少一个）：`keyword` 搜标题，或 `userId` 过滤作者。
3. 统一分页返回结构（二选一，固定一种写进文档）：
   - `ApiResult` 内嵌 `PageResult`（`items + total + page + size`），或
   - 直接返回 Spring `Page` 序列化结果（不推荐长期用，先理解即可）。

**验收**

- `page/size` 非法时有稳定 400（Validation 或业务校验均可）。
- 大数据量下 SQL 仍带 `limit`（看 SQL 日志确认）。

---

## Day 178（周三）— OpenAPI / Swagger UI：让人能「自助联调」

**学什么**

- **springdoc-openapi**：与 Spring MVC 的整合、与 Boot 3 的版本匹配。
- 注解最小集：`@Tag`、`@Operation`、`@Schema`、常见 **HTTP 401/400** 在文档中的表达。

**做什么**

1. 引入 `springdoc-openapi-starter-webmvc-ui`（版本与 Boot 3 对齐）。
2. 给 **Auth + Posts + Comments + Like** 的核心接口补文档注解（不必全覆盖，但主路径要齐）。
3. 说明 **Session 登录** 在 Swagger 里怎么测（Cookie / JSESSIONID；写进 README）。

**验收**

- 本地打开 UI（通常是 `/swagger-ui.html` 或 `/swagger-ui/index.html`，以依赖版本为准），能发出请求并得到与 `curl` 一致的结果。

---

## Day 179（周四）— Actuator：健康检查与「运维视角」的配置

**学什么**

- `spring-boot-starter-actuator`：`health`、`info`、就绪/存活（`/actuator/health`）。
- **生产默认值**：不要随便 `expose-all`；区分 **management 端口**（可选）与安全边界。

**做什么**

1. 引入 actuator，配置 **仅暴露** `health`、`info`（按需再加 `metrics`）。
2. 增加自定义 **`/actuator/info`**：应用名、构建时间或 git（可选）。
3. 若你已接数据库：**验证 `health` 能反映数据源**（down/up）。

**验收**

- `curl`/浏览器访问健康端点能得到预期 JSON；未发现把敏感 endpoints 全开的情况。

---

## Day 180（周五）— 配置绑定： `@ConfigurationProperties` + 本地秘钥不落库

**学什么**

- 类型安全的配置：**`@ConfigurationProperties`** + `@EnableConfigurationProperties` 或 `@ConfigurationPropertiesScan`。
- 外部化配置优先级：`application.yml` < `application-{profile}.yml` < 环境变量。

**做什么**

1. 抽一个 **`AppProperties`**：`api.default-page-size`、允许的 `cors.origins`、`session.cookie` 同名策略（选做）等。
2. 文档化：`DB_PASSWORD` 等如何通过环境变量注入（和你现在 `spring-boot:run` 用法一致）。
3. `.gitignore` 增补：`application-local.yml`、`.env`（如果你采用）。

**验收**

- 改配置文件或环境变量，`page/size` 默认值行为随之变化（写一个小断言或冒烟脚本验证）。

---

## Day 181（周六）— JDK 21 小步落地：records / 可读性更强的代码

**学什么**

- `record` 作为 **不可变 DTO** 的收益与边界（Jackson 序列化注意构造参数名与 JSON 字段对齐）。
- `switch` / `instanceof pattern`：**少而精**地使用，避免炫技污染业务代码。

**做什么**

1. 选 **2～3 个纯 DTO** 改为 `record`（优先 `RegisterRequest`、`MeResponse`、`PostResponse` 这类）。
2. 任选一处：**用更清晰的分支**替换冗长 `if`（例如根据 `BizException#getCode()` 映射 HTTP 状态，若你已集中处理可略）。
3. 跑一次 `mvn -q test`，修复 Jackson/校验注解迁移问题（record 上使用 `jakarta.validation` 的注意点）。

**验收**

- 编译与测试全绿；对外 JSON 兼容（旧 `curl`/前端字段不变）。

---

## Day 182（周日）— 集成测试收口：Testcontainers（推荐）+ 这周交付清单打包

**学什么**

- **Testcontainers**：在 CI/本机一致性跑 MySQL；与 Spring Boot Test 的组合。
- 测试边界：`@SpringBootTest` 的成本与不滥用原则。

**做什么**

1. 选择一路线并写进 README：
   - **A（推荐）**：Testcontainers MySQL + 一条 `@SpringBootTest` **薄集成**（只做主链路：注册→发帖→列表一笔）。
   - **B**：`application-test.yml` 指向单独测试库 + 初始化脚本（无 Testcontainers）。
2. 补充：`mvn -q verify` 能在本机重复通过（至少在无网络限制的本地环境）。
3. 更新 `README`：**W26 新增能力一览**（MP、分页、Swagger、Actuator、测试策略）。

**验收**

- 新加的集成测试在无手工启 MySQL（若用 Testcontainers）时也可跑通；若没有 Docker，则 README 写明走 **B** 的前置条件。

---

## 本周交付清单（必须）

| 交付物 | 说明 |
|--------|------|
| `boot-social-demo` 增量提交 | MP + 分页 +（可选）部分表迁移 |
| OpenAPI UI 可访问 | 主接口有文档注解 |
| Actuator health/info | 暴露策略合理 |
| `@ConfigurationProperties` | 至少 1 个业务配置类 |
| JDK 21 语法小落地 | record 或小重构其一 |
| 集成测试路线 | Testcontainers **或** 文档化测试库方案 |
| `W26-notes.md`（可选但很值） | 本周坑点：MP 分页、Swagger Session、 actuator 端口 |

---

## 与后续周衔接（W27–W28）

- **Redis**：热点缓存、会话外置等在 W27 接入更符合「为项目引入中间件」的节奏。
- **Docker / Nginx / GitLab 协作流**：放在 W27–W28 做「可上线」收口；W26 的 Actuator/Testcontainers 是在给那两周打底。

---

## 现在开始：Day 176

1. 接入 **MyBatis-Plus（Boot 3 starter）** + **分页插件**。  
2. 任选 **`UserMapper` 或 `PostMapper`** 之一改为继承 **`BaseMapper`**，并保持现有 API 行为不变。  
3. `mvn -q test`。  

做完把：**`pom.xml` MP 依赖段**、`MybatisPlusConfiguration`（或等价配置）截图/贴代码，以及 **一条你认为最容易冲突的 Mapper** 处理方式发我；我再带你 Day 177 把 **`GET /api/posts` 分页**写成「可长期维护」的返回结构。
