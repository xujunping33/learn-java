# W31 Notes — Day211（路径 A：MinIO / 对象存储）

## 本周取舍一句话
不要把图片/大文件塞进 MySQL `BLOB`；改为：**上传到 MinIO，对 MySQL 只存对象的 `objectKey`（元数据）**，读时通过服务端生成可访问 URL（或 presigned URL）。

## 3 个优点（做 MinIO 的理由）
1. **适合大文件/图片**：对象存储按 `key` 存取，吞吐和存储模型更贴合“文件内容”本身。
2. **把存储从业务主库拆开**：MySQL 只负责强一致的结构化数据（帖子、用户、权限、外键）；文件生命周期由对象存储管理。
3. **S3 兼容易落地**：MinIO 提供本地可复现环境，后续可以无痛迁移到 S3 同类 API，开发/讲解成本低。

## 3 个缺点（需要主动规避的坑）
1. **一致性不是一个事务**：上传对象与写 MySQL `objectKey` 不是同一事务，需要设计失败回滚/重试/补偿策略（至少文档化）。
2. **安全与权限要单独考虑**：不能把 `accessKey/secretKey` 暴露给浏览器；对外读通常用 `private bucket + presigned GET` 或服务端代理下载。
3. **运维与成本维度不同**：要关注 bucket 策略、生命周期（清理孤儿对象）、带宽/存储成本与日志追踪，而不只是数据库备份。

## 本周落地需求（给验收用的“用户可见一句话”）
为帖子增加封面上传：**`POST /api/posts/{id}/cover` 上传封面（限制 jpg/png 大小/类型），详情 `GET /api/posts/{id}` 返回 `coverUrl`（短期可访问）。**

## 最小 DDL 字段（Day214 写入 MySQL 元数据）
在 `posts` 表新增（只做最小可用元数据）：
1. `posts.cover_object_key VARCHAR(255) NULL`：MinIO 对象 key
> 可选（如果你想演示得更完整）：`posts.cover_content_type VARCHAR(100) NULL` / `posts.cover_updated_at DATETIME`

## 接口路径草案（Day214~Day215）
1. 写入：`POST /api/posts/{id}/cover`
   - `multipart/form-data`，字段建议：`file`
   - 成功：更新 `posts.cover_object_key`
2. 读取：在帖子详情中补 `coverUrl`
   - 如果采用 `presigned GET`：`coverUrl` 是短期 URL
   - 如果采用服务端转发：可给出 `GET /api/files/{key}`（这里二选一即可）

## 验收点（2 分钟能讲清）
- 为什么不用 MySQL `BLOB`：主库承担大对象会放大备份/迁移/IO、影响缓存与索引效率；并且权限与 CDN 不友好。
- trade-off：对象存储解决“内容”，MySQL 解决“关系与一致性约束”；两者配合需要补偿/权限设计。

## Day213（SDK + 配置）
- `pom.xml`：`software.amazon.awssdk:s3`（`S3Presigner` 含于该模块，独立 coordinate `s3-presigner` 在 Central 上不可用/非本 BOM 项）。
- `MinioProperties`（`app.minio.*`）+ `MinioS3Configuration`：`S3Client` + `S3Presigner`，path-style，endpoint 指向 MinIO。
- `StorageService`：`putObject`、`getObjectBytes`、`presignedGetUrl`。Docker：`application-docker.yml` + compose 注入 `MINIO_*`。
- Dev 冒烟：`POST /api/dev/storage/smoke`（需 `docker` profile 且 MinIO 可达）。

## Day214（业务：帖子封面）
- DDL：`posts.cover_object_key VARCHAR(255) NULL`
- 上传：`POST /api/posts/{id}/cover`（`multipart/form-data`，字段 `file`，仅作者可改；jpg/png；max 5MB）
- 详情：`GET /api/posts/{id}` 返回 `coverUrl`（presigned GET；无封面则 null）

## Day216（加固：失败补偿与排障）
- 上传链路不是单事务：对象上传与 MySQL 更新分属两套系统；若 DB 更新失败会产生“孤儿对象”
- 当前实现：上传后写 DB；若写 DB 抛异常则**尝试 deleteObject(objectKey)** 做最小补偿；补偿失败会打 warn（日后可再做定时清理）
- 日志字段：`cover_upload_ok/failed` 统一带 `uploadId/postId/objectKey/contentType/size`，方便定位单次上传

## Day217（收口复盘：3 个问题）
1. **为什么选 MinIO（而不是把文件塞 MySQL）**  
   - MySQL 更适合结构化数据与事务；文件/图片放进主库会放大备份、迁移、IO 与成本  
   - 对象存储更贴合“内容分发”（key + HTTP），后续可配 CDN / 生命周期策略  
   - MinIO 本地可复现，S3 兼容便于迁移到云
2. **生产还差什么（各一句）**  
   - HTTPS、鉴权/授权（谁能拿到 coverUrl）、更严格的上传校验（魔数、病毒扫描）  
   - CDN、对象生命周期/版本、审计与监控（上传失败率、外链访问量）  
   - 可靠性：更完整的孤儿对象清理（定时任务/对账）、限流与防刷
3. **如果流量 ×10，先优化哪里**  
   - 读取侧：减少后端带宽（presigned GET + CDN），合理 TTL 与缓存  
   - 上传侧：限制大小/并发、分片上传（需要时）、异步处理（缩略图/转码）  
   - 观测：日志 + 指标（成功率、延迟、失败原因分布）先把瓶颈定位清楚

