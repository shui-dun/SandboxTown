# CLAUDE.md

**在开始任何工作前，请先读取 [README.md](README.md) 获取项目介绍、技术栈和构建运行方法。**
本文档包含 README 中没有的架构细节、数据模型说明和 AI 助手特有的规范要求。

## 项目结构

```
SandboxTown/
├── sandbox_town_frontend/     # Vue 3 前端（Phaser.js + Matter.js 物理引擎）
│   ├── src/
│   │   ├── components/        # Vue组件（登录、背包、装备、商店、融合等面板）
│   │   ├── views/             # 页面视图（GameView 主游戏页、HomeView 首页）
│   │   ├── js/                # 核心 JS（MainScene 主场景、WebSocket、mitt事件总线、常量）
│   │   ├── router/            # Vue Router hash模式路由
│   │   └── assets/            # 静态资源（PNG图片素材、碰撞形状JSON）
│   ├── public/                # 公共静态文件
│   ├── nginx.template.conf    # Nginx 配置模板
│   └── vue.config.js          # Vue CLI 配置（开发代理将 /rest 转发到后端9090端口）
├── sandbox_town_backend/      # SpringBoot 2.7.x 后端（Java 21+）
│   └── src/main/java/com/shuidun/sandbox_town_backend/
│       ├── bean/              # 数据对象（DO + BO + VO/DTO）
│       ├── config/            # 配置类（Sa-Token认证、WebSocket、Redis、MyBatis-Plus）
│       ├── controller/        # REST 控制器（User/Building/Sprite/Tree/Chat）
│       ├── enumeration/       # 枚举类（建筑类型、精灵类型、效果、物品等）
│       ├── exception/         # 全局异常处理（BusinessException + GlobalExceptionHandler）
│       ├── mapper/            # MyBatis Mapper 接口
│       ├── mixin/             # 全局常量(Constants)和游戏缓存(GameCache)
│       ├── schedule/          # 定时任务（GameLoop游戏主循环、EventHandler事件处理、ChatScheduler聊天清理）
│       ├── service/           # 业务逻辑层
│       ├── utils/             # 工具类（Concurrent并发、DataCompressor压缩、MyMath数学、
│       │                      #   PasswordEncryptor密码加密、SecureNameGenerator/UUIDNameGenerator名称生成）
│       └── websocket/         # WebSocket（MyWebSocketHandler处理器、MyWebSocketInterceptor拦截器、
│                              #   WSMessageSender消息发送器）
├── sandbox_town_db/           # 数据库初始化SQL（import.sql）、MariaDB配置
├── scripts/                   # 开发辅助脚本，使用uv管理Python依赖
│   ├── image/                 # 使用阿里云 DashScope API 生成游戏素材图片
│   ├── auto_doc/              # 自动从import.sql解析生成doc/目录下的Markdown文档
│   └── physics_editor_helper/ # 物理编辑器辅助工具（碰撞形状生成）
├── doc/                       # 游戏文档（建筑、精灵、效果、物品、融合公式）
├── docker-compose.yml         # Docker Compose 基础配置（4个服务：front/back/mysql/redis）
├── docker-compose.dev.yml     # 开发环境覆盖配置（热部署、暴露调试端口）
└── docker-compose.prod.yml    # 生产环境覆盖配置
```

## 技术架构要点

### 游戏核心循环
游戏主循环位于 [GameLoop.java](sandbox_town_backend/src/main/java/com/shuidun/sandbox_town_backend/schedule/GameLoop.java)，以 50ms 为间隔运行（20 FPS）。每帧处理：精灵缓存失效 → 用户输入事件 → 精灵交互 → 精灵决策/移动 → 被动效果（生命回复、烧伤、饱腹值、体力恢复）→ 时间系统切换。

### WebSocket 通信
所有实时游戏数据通过 WebSocket 推送，包括精灵移动、物品变更、HP 变化、属性变化等。HTTP REST 仅用于非实时操作（登录、注册、查询静态数据等）。WebSocket 消息格式使用 JSON，通过 `WSRequestEnum` 和 `WSResponseEnum` 枚举区分消息类型

### 精灵 AI（SpriteAgent）
每种精灵类型实现 `SpriteAgent` 接口（位于 `SpriteService` 内部）。有SpriteStatus状态枚举：`IDLE/GO_TO_SPRITE/GO_TO_COORDINATE/GO_TO_BUILDING/INTERACTING`。移动通过 `MoveBo` 构建决策，`MapService.move()` 基于 BFS 计算路径。

### 地图系统
- 程序化生成：`game_map` 表存种子，内存生成 `GameMapBo`（`int[][]` 瓦片数组），每格30像素
- 位标记（`MapBitEnum`）标记建筑/树木/精灵等
**生态系统**：`ecosystem` 和 `ecosystem_type` 表定义地图上的生态区域（城镇 TOWN、迷宫 MAZE），影响建筑和精灵的生成分布。

### 时间系统（昼夜循环，全天=10分钟）
| 时段 | 时长 | 行为 |
|------|------|------|
| DAY | 5分钟 | 自由探索 |
| DUSK | 1分钟 | 部分精灵刷新 |
| NIGHT | 3分钟 | 夜行怪物刷新 |
| DAWN | 1分钟 | 夜行精灵受BURN烧伤，商店进货 |

各时段时长由 Constants.java 定义，精灵刷新按时段配置（`sprite_refresh` 表）

## 数据层（DO → BO → VO/DTO）

**DO（Data Object）类**（全部使用 Lombok `@Data`）
**BO（Business Object）类**（继承对应 DO，`@ToString(callSuper=true)`）
**VO/DTO 类**


核心实体关系：user→sprite(owner)，sprite→sprite_type，item→item_type，building→building_type，item_type关联label/attribute/effect三张表。

## 服务层要点

| 服务 | 职责 |
|------|------|
| `SpriteService` | 在线精灵管理、战斗、驯服、AI调度、NPC刷新 |
| `MapService` | 地图生成、BFS路径规划、建筑/生态生成 |
| `ItemService` | 物品CRUD、装备/手持/背包/物品栏位置管理 |
| `StoreService` | 商店买卖（实现 `SpecificBuildingService` 接口）|
| `FusionService` | 物品融合配方检查和执行 |
| `EventHandler` | WebSocket消息路由分发 |
| `SpecificBuildingService` | 建筑交互统一接口 |

## 前端架构

- **技术栈**：Vue 3 Options API + Phaser 3 + Bootstrap 5
- **事件总线**：mitt（`emitter.js`）
- **HTTP**：基于 fetch 的手动封装（`mixin.js`：`myPOSTJSON`/`myGET`/`myPOSTUrlEncoded`），因此不要手动写fetch了
- **路径别名**：`@` → `src/`

### 三种通信路径
1. **Props 向下**：GameView → 子组件
2. **Events 向上**：子组件 `$emit` → GameView
3. **跨级**：Phaser→Vue（`game.events.emit('forward')`）、WS→组件（mitt 事件总线）、全局函数（`mixin.fadeInfoShow`）

### 核心 JS 模块

| 文件 | 职责 |
|------|------|
| `MainScene.js` | Phaser 主游戏场景（地图渲染、精灵创建、移动/碰撞/交互、WebSocket事件处理） |
| `StoreScene.js` | 商店场景占位（空壳，preload/create/update均为空） |
| `websocket.js` | WebSocket 单例客户端（指数退避重连 1s→30s上限，消息通过 mitt 广播） |
| `mitt.js` | 事件总线实例（mitt库） |
| `mixin.js` | 全局工具函数库（HTTP封装 `myPOSTJSON`/`myGET`等、名称哈希 `hashName`） |
| `constants.js` | 常量定义（`ITEM_LABELS` 物品分类标签数组） |

## 游戏系统关键数据

### 物品系统


**物品位置**（`ItemPositionEnum`）

**物品标签**（`ItemLabelEnum`）

**物品操作**（`ItemOperationEnum`）：

**物品属性**：物品等级（1-10）影响增益数值，物品耐久度/寿命（0-100，-1表示无限耐久和可堆叠）。


### 战斗系统
- **伤害** = (攻击方attack + 增加值) - (防御方defense + 增加值)
- **限制**：不可攻击自己的宠物，火焰护体有反伤等
- **击杀奖励**：经验+金钱（`victory_attribute_reward`表）+ 物品掉落（`victory_item_reward`表）
**驯服系统**：通过喂养特定物品（`feed` 表配置），有概率（tameProb）驯服精灵。被驯服的精灵的 `owner` 字段指向主人。

### 精灵属性规范（`SpriteService.normalizeAndUpdateSprite()`）
HP[0,100]，等级[1,20]，每级100经验，速度上限25，饱腹值[0,100]阈值80。死亡惩罚：玩家扣120金钱+经验清零+传回原点；非玩家精灵删除。

更新属性后必须调用该方法进行规范化

### 效果系统
效果存在 effect 表。NOTHINGNESS(虚无)/LIFE(生命)/BURN(烧伤)/FLAME_BODY(火焰护体)等。精灵的当前效果存 `sprite_effect` 表，装备效果实时计算。

### 融合系统（Fusion）
物品融合在工厂建筑（FACTORY）处进行。玩家选择背包中的物品后，系统检查是否匹配融合配方（`fusion` + `fusion_material` 表）。匹配成功则扣除材料、生成合成物品。

### 商店系统（Store）

**定价机制**：物品有基础价格（`item_type.basic_price`），各商店实际价格围绕基础价格浮动，受商店等级影响。

**进货机制**：每天早上（DAWN）进货，物品刷新概率与稀有度（rarity）相关。


## 聊天系统
预定有消息类型、好友关系、自动清理、文件上传等功能，但尚未实现

## 并发

使用Concurrent.java提供的自定义线程池

## 缓存

- 不重要的东西，例如精灵缓存信息，存储在内存中（`SpriteService.onlineSpriteMap`），定期&异步写入数据库 
- 对于重要的数据，使用旁路缓存：**Redis**：Jackson2JsonRedisSerializer，5分钟默认过期，Spring Cache注解
- `GameCache` 类存储共享随机数生成器（`public static Random random`）
- **数据压缩**：`DataCompressor` 用于压缩地图等大数据

## 对 AI 助手的要求

以下要求是项目级别的规范，必须严格遵守。

- **不要无故删除任何已有注释**
- **所有注释必须使用中文**
- **代码必须包含详细注释**。每个类、每个公开方法、每个复杂逻辑块都必须有中文注释说明其用途和逻辑。注释的目的是方便人类审查和理解代码，宁可多写不可少写。
- 所有项目级别的配置文件，即使是给AI看的（CLAUDE.md、skills、README 等）必须使用中文编写，以方便人类审查。
- **所有对 AI 助手的行为要求必须存储在项目文件中**（如 `CLAUDE.md`、`.claude/skills/` 目录下的技能文件等）。这些文件必须位于 git 仓库可管理的路径下，确保版本可控、团队共享。（即不要写入 `~/.claude/projects/.../memory/` `~/.claude/CLAUDE.md`目录）存储项目级别的要求。
- 当用户提到 **"安装 skill"** 或 **"安装技能"** 时，指的是创建项目级别的 `.claude/skills/` 目录下的技能文件（Markdown 格式的指令文件）。**不要误解为安装 MCP 或 Claude Code Plugin**。
- Java 使用 Lombok（`@Slf4j`、`@Data`、`@NoArgsConstructor`、`@AllArgsConstructor`）
- **Bean层**：新增DO需在 `import.sql` 加建表语句+种子数据；字段变更同步更新BO/VO/DTO
- **枚举**：值变更同步更新 `import.sql` 中的引用数据
- **数据库变更**：修改 `import.sql` 后运行 `scripts/main.py` auto_doc 重新生成文档
