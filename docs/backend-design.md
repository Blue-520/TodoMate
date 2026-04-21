# TodoMate 后端设计方案

## 1. 目标

TodoMate 目前只有本地 `Room` 存储。下一阶段需要把数据同时保存在本地和后端，并支持多端同步。这个方案优先满足以下目标：

- App 继续保持离线可用，本地依然是第一读写入口。
- 后端作为统一数据源，负责持久化、备份、跨设备同步。
- 第一阶段先解决单用户、多设备同步，不做复杂多人协作。
- 架构上保留后续扩展空间，比如推送提醒、共享清单、统计报表。

## 2. 当前项目现状

从现有代码看，数据层仍处于非常早期阶段：

- 本地已有 `TaskEntity`、`CategoryEntity` 和 `Room Database`。
- `TaskEntity` 已包含标题、描述、优先级、开始/结束时间、通知时间等字段。
- 远端层只有一个空的 `TaskRequest` DTO，占位尚未接入。
- `DAO` 定义还不完整，方法签名中有明显占位和错误写法，说明还没正式进入业务实现阶段。

这意味着现在非常适合先把“本地模型如何映射到后端模型”和“同步机制怎么设计”定下来，否则后面很容易推倒重来。

## 3. 总体架构

建议采用“离线优先 + 增量同步”的架构。

### 3.1 架构原则

- Android 端本地数据库是用户操作的直接落点。
- 所有新增、修改、删除操作先写本地，再进入同步队列。
- 后端保存用户的完整任务数据，并提供增量拉取接口。
- 同步由客户端主动触发，后续再根据需要补充推送或 WebSocket。

### 3.2 逻辑分层

Android:

- UI 层：Compose / ViewModel
- Domain 层：UseCase，可选
- Data 层：
  - `local`：Room
  - `remote`：Retrofit/Ktor Client
  - `repository`：统一协调本地和远端
  - `sync`：同步任务调度、冲突处理、重试

Backend:

- API 层：REST API
- Service 层：任务、分类、同步、用户服务
- Persistence 层：MySQL/PostgreSQL
- Auth 层：JWT / Session Token

## 4. 推荐后端技术选型

如果目标是快速落地，并且和 Android/Kotlin 技术栈保持一致，优先推荐：

- 后端语言：Kotlin
- 框架：Spring Boot
- 数据库：PostgreSQL
- 鉴权：JWT
- 部署：Docker + Nginx

原因：

- Kotlin 与 Android 共用一套语言，DTO、字段命名、时间处理习惯更一致。
- Spring Boot 生态成熟，认证、数据库、日志、监控、对象校验都比较完整。
- PostgreSQL 对时间、索引、JSON、事务支持稳定，适合任务和同步场景。

如果你更想追求“快写快跑”，也可以用 Node.js + NestJS，但结合当前项目是 Android Kotlin，Kotlin 后端更顺手。

## 5. 核心业务模型

第一阶段建议后端至少有 4 张核心表：

- `users`
- `categories`
- `tasks`
- `sync_operations` 或 `device_sync_state`

### 5.1 users

用途：用户账号体系。哪怕第一阶段只是手机号/邮箱登录，也建议先把用户表立起来。

建议字段：

- `id`: UUID 或 bigint
- `email`: 可空，唯一
- `phone`: 可空，唯一
- `password_hash`: 可空，如果后面走短信验证码登录可放宽
- `nickname`
- `created_at`
- `updated_at`
- `last_login_at`

### 5.2 categories

用途：任务分类，如“工作”“学习”“生活”。

建议字段：

- `id`: UUID
- `user_id`
- `name`
- `color`: 可空，前端可选
- `sort_order`: 可空
- `is_deleted`: boolean
- `created_at`
- `updated_at`
- `deleted_at`: 可空

说明：

- 分类属于用户私有数据，必须带 `user_id`。
- 删除建议先做软删除，避免多端同步时直接丢失。

### 5.3 tasks

用途：任务主体。

建议字段：

- `id`: UUID
- `user_id`
- `category_id`: 可空
- `title`
- `description`: 可空
- `status`: `TODO` / `DONE` / `CANCELLED`
- `priority`: 可空，建议 1-5
- `start_time`: 可空
- `end_time`: 可空
- `is_notify_enabled`
- `notify_at`: 可空
- `client_created_at`: 客户端首次创建时间，可空
- `created_at`
- `updated_at`
- `deleted_at`: 可空
- `is_deleted`
- `version`: bigint

说明：

- `status` 比 `isComplete` 更可扩展。
- `notify_hour` + `notify_minute` 可以在服务端统一收敛为一个 `notify_at` 或规则字段。
- `version` 用于乐观锁或同步冲突处理。
- `updated_at` 是增量同步最关键的字段之一。

### 5.4 device_sync_state

用途：记录每台设备上次同步到什么位置，方便增量拉取。

建议字段：

- `id`
- `user_id`
- `device_id`
- `last_sync_token`
- `last_sync_at`
- `app_version`
- `platform`

如果第一阶段想简单一些，也可以不单独建表，而是客户端自己保存最后同步时间戳。但从长期看，后端保留设备状态更稳妥。

## 6. Android 本地数据模型改造建议

现有本地表不能直接用于多端同步，需要补同步字段。建议本地表结构向“服务端实体 + 本地同步状态”演进。

### 6.1 CategoryEntity 建议新增字段

- `serverId: String?`
- `userId: String?`
- `color: String?`
- `sortOrder: Int`
- `isDeleted: Boolean`
- `createdAt: Long`
- `updatedAt: Long`
- `syncState: SyncState`

### 6.2 TaskEntity 建议新增字段

- `serverId: String?`
- `userId: String?`
- `status: String`
- `notifyAt: Long?`
- `isDeleted: Boolean`
- `updatedAt: Long`
- `version: Long`
- `syncState: SyncState`

### 6.3 syncState 建议枚举

- `LOCAL_ONLY`: 本地新建，服务端还没有
- `SYNCED`: 已同步
- `PENDING_UPDATE`: 本地改过，待上传
- `PENDING_DELETE`: 本地删除，待同步
- `SYNC_FAILED`: 同步失败，待重试

说明：

- 不建议直接依赖本地自增主键作为远端主键。
- 本地主键可继续用 `Long`，同时单独保存 `serverId`。
- 新数据在上传成功后，把后端返回的 `serverId` 回写本地。

## 7. 接口设计

第一阶段用 REST 即可，不需要一开始就上 GraphQL 或 WebSocket。

接口统一前缀建议：

- `/api/v1/auth`
- `/api/v1/categories`
- `/api/v1/tasks`
- `/api/v1/sync`

### 7.1 认证接口

#### `POST /api/v1/auth/register`

请求：

```json
{
  "email": "demo@test.com",
  "password": "12345678",
  "nickname": "Blue"
}
```

响应：

```json
{
  "userId": "u_123",
  "accessToken": "jwt-token",
  "refreshToken": "refresh-token"
}
```

#### `POST /api/v1/auth/login`

#### `POST /api/v1/auth/refresh`

### 7.2 分类接口

#### `GET /api/v1/categories`

返回当前用户所有未删除分类。

#### `POST /api/v1/categories`

请求：

```json
{
  "name": "工作",
  "color": "#4A90E2",
  "sortOrder": 1,
  "clientRequestId": "c1f1..."
}
```

#### `PUT /api/v1/categories/{id}`

#### `DELETE /api/v1/categories/{id}`

注意：

- 删除建议做软删除。
- 如果分类下仍有任务，需要明确策略：设为空分类、迁移到默认分类，或禁止删除。第一阶段建议删除分类后把任务 `category_id` 置空。

### 7.3 任务接口

#### `GET /api/v1/tasks`

支持筛选参数：

- `updatedAfter`
- `categoryId`
- `status`
- `page`
- `pageSize`

#### `GET /api/v1/tasks/{id}`

#### `POST /api/v1/tasks`

请求：

```json
{
  "title": "写周报",
  "description": "整理本周完成项",
  "categoryId": "cat_001",
  "status": "TODO",
  "priority": 3,
  "startTime": 1776624000000,
  "endTime": 1776710400000,
  "isNotifyEnabled": true,
  "notifyAt": 1776620400000,
  "clientRequestId": "req_001",
  "clientUpdatedAt": 1776610000000
}
```

#### `PUT /api/v1/tasks/{id}`

请求中建议带：

- `version`
- `clientUpdatedAt`

这样后端可以做冲突检查。

#### `DELETE /api/v1/tasks/{id}`

服务端执行软删除。

### 7.4 增量同步接口

这是整个方案里最关键的接口。

#### `POST /api/v1/sync/pull`

请求：

```json
{
  "deviceId": "android_abc",
  "lastSyncToken": "2026-04-20T12:00:00Z"
}
```

响应：

```json
{
  "nextSyncToken": "2026-04-20T13:00:00Z",
  "categories": [
    {
      "id": "cat_001",
      "name": "工作",
      "isDeleted": false,
      "updatedAt": "2026-04-20T12:30:00Z",
      "version": 2
    }
  ],
  "tasks": [
    {
      "id": "task_001",
      "title": "写周报",
      "status": "DONE",
      "isDeleted": false,
      "updatedAt": "2026-04-20T12:50:00Z",
      "version": 4
    }
  ]
}
```

#### `POST /api/v1/sync/push`

请求：

```json
{
  "deviceId": "android_abc",
  "operations": [
    {
      "entityType": "TASK",
      "operationType": "CREATE",
      "clientId": "local_task_1",
      "clientRequestId": "req_001",
      "payload": {
        "title": "写周报",
        "status": "TODO"
      },
      "clientUpdatedAt": 1776610000000
    },
    {
      "entityType": "TASK",
      "operationType": "UPDATE",
      "serverId": "task_001",
      "version": 3,
      "payload": {
        "status": "DONE"
      },
      "clientUpdatedAt": 1776611111111
    }
  ]
}
```

响应：

```json
{
  "results": [
    {
      "clientRequestId": "req_001",
      "success": true,
      "serverId": "task_001",
      "version": 1
    },
    {
      "serverId": "task_001",
      "success": true,
      "version": 4
    }
  ],
  "serverTime": "2026-04-20T13:05:00Z"
}
```

说明：

- `clientRequestId` 用于幂等，避免重试时重复创建。
- `push` 负责上传本地变更。
- `pull` 负责拉取服务端变更。
- 第一阶段可以先做“先 push，再 pull”的同步顺序。

## 8. 同步策略设计

### 8.1 推荐策略：离线优先

用户操作流程：

1. 用户在 App 中新增或修改任务。
2. 先写入 Room。
3. 把该记录标记为 `PENDING_UPDATE` 或 `LOCAL_ONLY`。
4. 由后台同步任务把变更推送到后端。
5. 推送成功后回写 `serverId`、`version`、`updatedAt`，状态改为 `SYNCED`。
6. 再执行一次增量拉取，拿到其他设备的更新。

### 8.2 同步触发时机

建议触发点：

- 用户登录成功后
- App 冷启动后
- App 回到前台时
- 本地产生写操作后延迟几秒
- 网络从断开恢复为可用时
- 定时后台任务触发

Android 上建议使用：

- `WorkManager` 做可靠后台同步
- `Coroutine` 做前台轻量同步

### 8.3 冲突处理策略

第一阶段建议使用简单可控的策略：

- 以 `version` + `updatedAt` 判断是否冲突。
- 默认规则：后写覆盖前写，服务端返回最终版本。
- 如果同一条任务在两端都被修改：
  - 若字段冲突不频繁，直接采用 Last Write Wins。
  - 若你希望更稳妥，可在后续版本中做字段级合并。

推荐第一阶段结论：

- 先使用 `Last Write Wins`。
- 记录冲突日志，后续再评估是否升级为人工提示或字段级合并。

这不是最理想的协同方案，但实现成本最低，足够支撑 Todo 类应用第一版。

### 8.4 删除策略

不建议真删，统一软删除：

- 本地删除：标记 `isDeleted = true`，`syncState = PENDING_DELETE`
- 服务端删除：写入 `deleted_at`
- 拉取同步时，如果发现服务端已删除，本地也同步删除或隐藏

原因：

- 软删除更适合多端同步
- 便于恢复
- 避免一台设备删了，另一台设备还没同步时出现错乱

## 9. 安全与鉴权

第一阶段建议最少做到：

- HTTPS 全站启用
- 用户登录后发 `accessToken` + `refreshToken`
- 每个接口根据 `userId` 做数据隔离
- 服务端严禁通过客户端传入的 `userId` 直接信任数据归属
- 关键写接口增加幂等处理

额外建议：

- 密码使用 `bcrypt` 或 `argon2`
- 刷新令牌单独存表，可做失效管理
- 日志中不要打印密码、token、完整隐私信息

## 10. 版本一的最小可交付范围

建议先做 MVP，不要一口气把协同、共享、推送、统计都拉进来。

### 10.1 后端 MVP

- 用户注册/登录
- 分类 CRUD
- 任务 CRUD
- 软删除
- 基于 `updatedAt` 的增量拉取
- 基于 `clientRequestId` 的幂等创建
- JWT 鉴权

### 10.2 Android MVP 改造

- 本地表增加 `serverId`、`updatedAt`、`isDeleted`、`syncState`、`version`
- 增加 `Repository`
- 增加 `RemoteDataSource`
- 接入 `WorkManager`
- 登录后触发全量首次同步
- 本地改动自动入同步队列

## 11. 推荐接口返回结构

为了便于统一处理，建议所有接口使用一致包裹结构：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

分页接口：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "items": [],
    "page": 1,
    "pageSize": 20,
    "total": 100
  }
}
```

错误返回：

```json
{
  "code": 40001,
  "message": "version conflict",
  "data": null
}
```

## 12. 推荐数据库表示意

下面给一版简化后的任务表定义思路：

```sql
create table tasks (
    id uuid primary key,
    user_id uuid not null,
    category_id uuid null,
    title varchar(255) not null,
    description text null,
    status varchar(32) not null default 'TODO',
    priority int null,
    start_time timestamptz null,
    end_time timestamptz null,
    is_notify_enabled boolean not null default false,
    notify_at timestamptz null,
    version bigint not null default 1,
    is_deleted boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    deleted_at timestamptz null
);

create index idx_tasks_user_updated_at on tasks(user_id, updated_at);
create index idx_tasks_user_deleted on tasks(user_id, is_deleted);
create index idx_tasks_category_id on tasks(category_id);
```

## 13. Android 侧推荐目录结构

后续客户端可以调整为：

```text
data/
  local/
    dao/
    database/
    entity/
  remote/
    api/
    dto/
  repository/
  sync/
domain/
  model/
  usecase/
ui/
```

其中：

- `entity` 用于 Room
- `dto` 用于网络传输
- `model` 用于业务层

不要直接让 UI 层同时依赖 Room Entity 和 Retrofit DTO，否则后期会很乱。

## 14. 开发顺序建议

建议按下面顺序推进，而不是同时乱铺：

1. 先修正本地数据层设计
2. 定义统一业务模型和 DTO
3. 搭建后端基础工程和认证
4. 完成分类/任务 CRUD
5. 实现 `push` / `pull` 同步接口
6. Android 接入远端 API
7. 增加后台同步和失败重试
8. 最后再补通知、统计、共享等增强能力

## 15. 我给你的明确建议

如果这是个人项目或课程项目，我建议你不要一上来做“在线直连 + 所有操作都走服务器”。

最稳的路线是：

- 本地 `Room` 保留
- 后端作为同步和备份中心
- 客户端采用离线优先
- 第一阶段只做账号 + 任务/分类 + 增量同步

这样做的好处：

- 用户体验最好，弱网下也能用
- Android 改造成本可控
- 后端复杂度不会失控
- 后续想加 Web 端、小程序端、桌面端时，服务端模型已经能复用

## 16. 下一步建议

如果你认可这版设计，下一步我建议直接做两件事：

1. 先把 Android 本地实体和 DAO 重构成“可同步”的结构
2. 我再给你继续产出一份后端 API 详细文档，或者直接开始搭一个 Spring Boot 后端骨架

---

文档基于当前仓库现状撰写，重点是先把数据模型和同步边界定义清楚，再进入编码阶段。
