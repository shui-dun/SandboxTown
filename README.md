# 🏠 沙盒小镇

多人网络沙盒游戏

## 技术选型

- 前端框架：Vue
- 游戏引擎：Phaser.js
- 通信协议：WebSocket & HTTP
- 后端框架：SpringBoot
- 持久层：MariaDB & MyBatis
- 缓存：Redis
- 安全框架：Sa-Token

## 构建方法

```bash
cp sandbox_town_backend/src/main/resources/application.template.yml \
   sandbox_town_backend/src/main/resources/application.yml
cp deploy/mysql/customer.template.cnf deploy/mysql/customer.cnf
cp deploy/vue/nginx.template.conf deploy/vue/nginx.conf
cp deploy/docker-compose.template.yml deploy/docker-compose.yml
# 按需修改上述几个配置文件，例如配置ssl证书位置、配置域名等
cd deploy
docker-compose up -d
```

## 游戏截图

请参见[游戏截图](doc/screenshot.md)

## 功能介绍

### 账号模块

- 玩家需要注册账号（提供用户名和密码）和登录账号才能进行游戏
- 可以保持登录7天，也可以不记住密码
- 玩家可以登出账号
- 具有admin权限的用户（管理员）可以封禁和解封普通玩家
- 登录奖励：如果是新用户或者超过30天没有上线，获得200元奖励；如果1天没有上线，获得40元奖励

### 地图与建筑模块

- 地图通过种子生成
- 地图背景具有草地、花朵等纹理图案，以及迷宫式墙壁
- 玩家可以查看自己的物品栏、效果栏、时间、背包、装备栏
- 玩家点击即可寻路前往终点，如果终点是建筑，到达后会触发相应事件，例如到达商店时打开商店页面

更多信息请参见[建筑列表](doc/building.md)

### 精灵模块

- 精灵包含玩家、宠物、怪物等
- 精灵有金钱、所有者、经验值、等级、饱腹值、血量、攻击、防御、速度、视野、攻击范围等属性
- 经验值达到一定水平后会升级，升级会永久提升自己各方面的属性值
- 游戏中的精灵饱腹值会定期减少
- 饱腹值大于一定值时定期恢复体力
- 精灵点击可以攻击其他精灵
- 有些精灵可以被驯养，例如狗可以被手持骨头的精灵驯养
- 有些精灵可以被喂食，增加饱腹值和经验值
- 玩家在死亡后经验值归零，扣除一定金钱，并回到出生点。但其他精灵死亡后会消失，其拥有的宠物也同时消失，拥有的建筑变为无主状态
- 玩家右键点击精灵可以查看其资料面板

更多信息请参见[角色列表](doc/sprite.md)和[效果列表](doc/effect.md)

### 物品模块

- 物品包含食物、装备、武器、用品等类别
- 物品可以放置在背包、物品栏、装备栏等位置
- 物品在被使用、被装备或手持可以为精灵带来属性增益（例如提高速度），或者特殊效果（例如火焰附体效果）
- 物品具有基础价格、稀有度、耐久度等属性

更多信息请参见[物品列表](doc/item.md)

### 时间系统

- 一天包括白天、黄昏、夜晚、黎明
- 每天白天刚开始时，会更新树上的苹果、商店中的商品等等

## 素材来源

- https://www.bing.com/create 所有建筑、角色、物品的素材
- https://cupnooble.itch.io/sprout-lands-asset-pack 背景纹理和围墙
- https://remixicon.com/ 箭头、搜索等图标

## License

[MIT](./LICENSE)
