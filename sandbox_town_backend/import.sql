# 删除数据库
DROP DATABASE IF EXISTS sandbox_town;

# 创建数据库
CREATE DATABASE sandbox_town;
USE sandbox_town;

# 创建用户表
CREATE TABLE user
(
    username     VARCHAR(255) NOT NULL PRIMARY KEY,
    password     VARCHAR(255) NOT NULL,
    salt         VARCHAR(255) NOT NULL,
    # 封禁结束时间
    ban_end_date DATETIME,
    # 作弊次数
    cheat_count  INT      DEFAULT 0,
    # 用户创建时间
    create_date  DATETIME DEFAULT CURRENT_TIMESTAMP,
    # 上次在线（进入游戏）时间
    last_online  DATETIME
);

# 密码都是123456
INSERT INTO user (username, password, salt)
VALUES ('USER_haha', '3ff432d13d5060159f9daf745c6c0c414624159bce95b32437e6e4c59211a144', 'I+zxIDZF1PJ/G/LGQrwtgw=='),
       ('USER_heihei', 'ed63891bacfd0861da88898ad002534f6d61058bce25c41e67b763a2d95f642a', 'ykzgDWYsa77gmD2bhMm41A=='),
       ('USER_xixi', '38232ca0c66eb3f33cc55696233fbf905fd02ceaad4242f5cd41b97b272c55d8', 'KaSg9wFMopkEaU/cDY+Xvg==');

# 创建用户权限表
CREATE TABLE user_role
(
    username VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL,
    PRIMARY KEY (username, role),
    CONSTRAINT fk_user_role_username FOREIGN KEY (username) REFERENCES user (username)
);


INSERT INTO user_role (username, role)
VALUES ('USER_haha', 'NORMAL'),
       ('USER_heihei', 'ADMIN'),
       ('USER_xixi', 'NORMAL');

# 创建地图表
CREATE TABLE game_map
(
    # 地图的名称
    id     VARCHAR(255) NOT NULL PRIMARY KEY,
    # 名称
    name   VARCHAR(255) NOT NULL,
    # 地图的宽度
    width  INT          NOT NULL,
    # 地图的高度
    height INT          NOT NULL,
    # 种子（用于生成随机迷宫等）
    seed   INT          NOT NULL
);

INSERT INTO game_map (id, name, width, height, seed)
VALUES ('1', 'Ⅰ', 4000, 3000, 32784924),
       ('2', 'Ⅱ', 4000, 3000, 234757802);

# 创建物品表
CREATE TABLE item_type
(
    # id 用于标识物品，比如 wood, stone
    id          VARCHAR(255) NOT NULL PRIMARY KEY,
    # name 用于显示物品名称，比如 木头，石头
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    # 注意这个价格只是参考价格，各个商店会有上下波动
    basic_price INT          NOT NULL DEFAULT 0,
    # 稀有度（稀有度越高，商店刷出的概率越高）
    rarity      INT          NOT NULL DEFAULT 0,
    # 耐久度（耐久度越高寿命越长，-1代表无限耐久，同时也代表可堆叠）
    durability  INT          NOT NULL DEFAULT -1
);

INSERT INTO item_type (id, name, description, basic_price, rarity, durability)
VALUES ('WOOD', '木头', '建筑的材料，也可处于烤火', 5, 10, -1),
       ('STONE', '石头', '用于建造房屋和其他工具', 8, 7, -1),
       ('BREAD', '面包', '具有松软的质地和微甜的口感', 5, 7, -1),
       ('APPLE', '苹果', '禁忌和知识之果', 5, 8, -1),
       ('TREASURE_CHEST', '宝箱', '打开宝箱，获得随机物品', 20, 1, -1);

# 创建角色类型表
CREATE TABLE sprite_type
(
    # 角色类型，比如 user, dog, cat, spider
    type          VARCHAR(255) NOT NULL PRIMARY KEY,
    # 角色名称，比如 玩家，狗狗，猫咪，蜘蛛
    name          VARCHAR(255) NOT NULL,
    description   VARCHAR(255) NOT NULL,
    # 注意这个价格只是参考价格，各个商店会有上下波动
    basic_price   INT          NOT NULL DEFAULT 0,
    # 基础金钱
    basic_money   INT          NOT NULL DEFAULT 0,
    # 基础经验值
    basic_exp     INT          NOT NULL DEFAULT 0,
    # 基础等级
    basic_level   INT          NOT NULL DEFAULT 1,
    # 基础饱腹值
    basic_hunger  INT          NOT NULL DEFAULT 100,
    # 基础生命值
    basic_hp      INT          NOT NULL DEFAULT 100,
    # 基础攻击力
    basic_attack  INT          NOT NULL DEFAULT 10,
    # 基础防御力
    basic_defense INT          NOT NULL DEFAULT 10,
    # 基础速度
    basic_speed   INT          NOT NULL DEFAULT 10,
    # 基础宽度
    basic_width   INT          NOT NULL DEFAULT 120,
    # 基础高度
    basic_height  INT          NOT NULL DEFAULT 120
);

INSERT INTO sprite_type (type, name, description, basic_price, basic_money,
                         basic_exp, basic_level, basic_hunger, basic_hp,
                         basic_attack, basic_defense, basic_speed, basic_width, basic_height)
VALUES ('USER', '玩家', '小镇居民', 0, 0, 0, 1, 100, 100, 10, 10, 10, 120, 120),
       ('DOG', '狗狗', '可靠的护卫，忠诚而勇敢，像你的影子一样一直陪伴着你', 0, 0, 0, 1, 100, 100, 10, 10, 10, 120, 120),
       ('CAT', '猫咪', '常见的家养宠物，具有柔软的毛发和灵活的身体，喜爱捕鱼', 0, 0, 0, 1, 100, 100, 10, 10, 10, 120,
        120),
       ('SPIDER', '蜘蛛', '八腿的恶棍，以其敏捷和毒液为武器', 0, 0, 0, 1, 100, 100, 10, 10, 10, 120, 120);

# 创建角色表，包含玩家、宠物、怪物等角色
CREATE TABLE sprite
(
    id      VARCHAR(255) NOT NULL PRIMARY KEY,
    # 类型
    type    VARCHAR(255) NOT NULL,
    # 主人
    owner   VARCHAR(255),
    # 角色的各个属性都是基础值，不包括装备、效果等加成
    money   INT                   DEFAULT 0,
    exp     INT          NOT NULL DEFAULT 0,
    level   INT          NOT NULL DEFAULT 1,
    hunger  INT          NOT NULL DEFAULT 100,
    hp      INT          NOT NULL DEFAULT 100,
    attack  INT          NOT NULL DEFAULT 10,
    defense INT          NOT NULL DEFAULT 10,
    speed   INT          NOT NULL DEFAULT 10,
    x       INT          NOT NULL DEFAULT 0,
    y       INT          NOT NULL DEFAULT 0,
    width   INT          NOT NULL DEFAULT 120,
    height  INT          NOT NULL DEFAULT 120,
    # 所在地图名称
    map     VARCHAR(255) NOT NULL,
    CONSTRAINT fk_sprite_type FOREIGN KEY (type) REFERENCES sprite_type (type),
    CONSTRAINT fk_sprite_map FOREIGN KEY (map) REFERENCES game_map (id),
    CONSTRAINT fk_sprite_owner FOREIGN KEY (owner) REFERENCES sprite (id)
);

INSERT INTO sprite (id, type, owner, money, exp, level, hunger, hp, attack, defense, speed, x, y, map, width,
                    height)
VALUES ('USER_xixi', 'USER', null, 10, 0, 1, 100, 100, 10, 10, 10, 300, 300, '1', 150, 150),
       ('USER_haha', 'USER', null, 10, 0, 1, 100, 100, 10, 10, 20, 100, 100, '1', 150, 150),
       ('USER_heihei', 'USER', null, 10, 0, 1, 100, 100, 10, 10, 20, 200, 200, '1', 150, 150),
       ('DOG_Vz5n_o-CQk-okcK5vQFRsA', 'DOG', 'USER_xixi', null, 10, 2, 70, 40, 8, 6, 8, 400, 300, '1', 150, 150),
       ('DOG_q83jrKyCTtGm1QvywN48pw', 'DOG', 'USER_xixi', null, 10, 2, 70, 40, 13, 6, 8, 400, 400, '1', 250, 250),
       ('CAT_iZUc8IiRTCOQXNjLNbQUFQ', 'CAT', 'USER_xixi', null, 10, 2, 70, 40, 8, 6, 8, 400, 500, '1', 150, 150);


# 创建效果表
create table effect
(
    id          VARCHAR(255) NOT NULL PRIMARY KEY,
    # 中文名称
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL
);

INSERT INTO effect (id, name, description)
VALUES ('NOTHINGNESS', '虚无', '虚无的存在，难以被攻击'),
       ('LIFE', '生命', '生命，能够治愈伤口');

# 角色的效果列表
# 只包含使用物品带来的效果以及其过期时间，装备的效果自己去查
create table sprite_effect
(
    sprite   VARCHAR(255) NOT NULL,
    effect   VARCHAR(255) NOT NULL,
    # 效果的持续时间
    duration INT          NOT NULL DEFAULT 0,
    primary key (sprite, effect),
    CONSTRAINT fk_sprite_effect_sprite FOREIGN KEY (sprite) REFERENCES sprite (id),
    CONSTRAINT fk_sprite_effect_effect FOREIGN KEY (effect) REFERENCES effect (id)
);

# 物品标签表
create table item_type_label
(
    item_type varchar(255) not null,
    label     varchar(255) not null,
    primary key (item_type, label),
    foreign key (item_type) references item_type (id)
);

insert into item_type_label
values ('BREAD', 'FOOD'),
       ('APPLE', 'FOOD'),
       ('APPLE', 'USABLE'),
       ('TREASURE_CHEST', 'USABLE');

# 装备、使用、或手持该物品后对角色各个属性的增益值
# 注意：这些增益值指的是等级为1的物品带来的增益值，等级越高，增益值越高
create table item_type_attribute
(
    item_type   varchar(255) not null,
    # 装备（equip）、使用（use）、或手持（handheld）
    # 装备指在装备栏放置helmet（头盔）, chest（胸甲）, leg（腿甲）, boots（鞋）
    # 使用指使用food（食品）、usable（用品）
    # 手持指手持物品
    operation   varchar(255) not null,
    # 增加金钱
    money_inc   INT          NOT NULL DEFAULT 0,
    # 增加经验值
    exp_inc     INT          NOT NULL DEFAULT 0,
    # 增加等级
    level_inc   INT          NOT NULL DEFAULT 0,
    # 增加饱腹值
    hunger_inc  INT          NOT NULL DEFAULT 0,
    # 增加生命值
    hp_inc      INT          NOT NULL DEFAULT 0,
    # 增加攻击力
    attack_inc  INT          NOT NULL DEFAULT 0,
    # 增加防御力
    defense_inc INT          NOT NULL DEFAULT 0,
    # 增加速度
    speed_inc   INT          NOT NULL DEFAULT 0,
    primary key (item_type, operation),
    foreign key (item_type) references item_type (id)
);

insert into item_type_attribute(item_type, operation, money_inc, exp_inc, level_inc, hunger_inc, hp_inc, attack_inc,
                                defense_inc,
                                speed_inc)
values ('BREAD', 'USE', 0, 0, 0, 10, 0, 0, 0, 0),
       ('APPLE', 'USE', 0, 4, 0, 4, 0, 0, 0, 0);


# 装备物品后对对角色带来的特殊效果
# 注意：这些效果的持续时间指的是等级为1的物品的效果持续时间，等级越高，持续时间越长
create table item_type_effect
(
    item_type varchar(255) not null,
    # 进行什么操作，例如装备、使用等
    operation varchar(255) not null,
    effect    varchar(255) not null,
    # 持续时间（秒）
    # -1 表示永久
    duration  INT          NOT NULL DEFAULT 0,
    primary key (item_type, operation, effect),
    foreign key (item_type) references item_type (id),
    foreign key (effect) references effect (id)
);

# 创建物品表
create table item
(
    id         varchar(255) not null primary key,
    owner      VARCHAR(255) NOT NULL,
    item_type  VARCHAR(255) NOT NULL,
    item_count INT          NOT NULL DEFAULT 1,
    # 寿命（0-100，100表示刚刚获得）
    life       INT          NOT NULL DEFAULT 100,
    # 等级（1-10，1表示刚刚获得）
    level      INT          NOT NULL DEFAULT 1,
    # 位置，包括背包等
    position   VARCHAR(255) NOT NULL,
    FOREIGN KEY (owner) REFERENCES sprite (id),
    FOREIGN KEY (item_type) REFERENCES item_type (id)
);

insert into item(id, owner, item_type, item_count, life, level, position)
values ('BREAD_jhdfiu', 'USER_xixi', 'BREAD', 1, 100, 1, 'BACKPACK'),
       ('APPLE_hdjfdjeio', 'USER_xixi', 'APPLE', 2, 100, 1, 'BACKPACK'),
       ('TREASURE_CHEST_ixdiue', 'USER_xixi', 'TREASURE_CHEST', 1, 100, 1, 'BACKPACK');


# 创建建筑类型表
CREATE TABLE building_type
(
    # 建筑的类型，例如store
    id           VARCHAR(255) NOT NULL PRIMARY KEY,
    # 描述信息
    description  VARCHAR(255) NOT NULL,
    # 建筑的基础价格
    basic_price  INT          NOT NULL DEFAULT 0,
    # 黑白图的路径
    image_path   VARCHAR(255) NOT NULL,
    # 基础宽度（真实宽度会在此基础上波动）
    basic_width  INT          NOT NULL DEFAULT 0,
    # 基础高度（真实高度会在此基础上波动）
    basic_height INT          NOT NULL DEFAULT 0,
    # 稀有度 (0-100)，越低越稀有
    rarity       INT          NOT NULL DEFAULT 0
);

INSERT INTO building_type (id, description, basic_price, image_path, basic_width, basic_height, rarity)
VALUES ('STORE', '买卖商品的场所', 200, 'static/bitmap/STORE.png', 400, 400, 15),
       ('TREE', '可以伐木或摘苹果', 100, 'static/bitmap/TREE.png', 500, 500, 40);


# 创建建筑表
CREATE TABLE building
(
    # 建筑的id
    id       VARCHAR(255) NOT NULL PRIMARY KEY,
    # 建筑的类型
    type     VARCHAR(255) NOT NULL,
    # 建筑所在的地图名称
    map      VARCHAR(255) NOT NULL,
    # 建筑的等级（等级越高，收益越好，例如商店等级越高，商品数目越多，商品价格越低，物品价格由其基本价格和商店等价和随机数共同决定）
    level    INT          NOT NULL DEFAULT 1,
    # 建筑的拥有者
    owner    VARCHAR(255),
    # 建筑左上角的x坐标（对于建筑，我们使用图像左上角的坐标，以方便寻路算法，但是对于玩家等，我们使用质心的坐标）
    origin_x INT          NOT NULL,
    # 建筑左上角的y坐标
    origin_y INT          NOT NULL,
    # 建筑的宽度
    width    INT          NOT NULL,
    # 建筑的高度
    height   INT          NOT NULL,
    CONSTRAINT fk_building_type FOREIGN KEY (type) REFERENCES building_type (id),
    CONSTRAINT fk_building_owner FOREIGN KEY (owner) REFERENCES sprite (id),
    CONSTRAINT fk_building_map FOREIGN KEY (map) REFERENCES game_map (id)
);

# 创建树表
CREATE TABLE tree
(
    # 树的id
    id               VARCHAR(255) NOT NULL PRIMARY KEY,
    # 苹果的数量
    apples_count     INT          NOT NULL DEFAULT 0,
    # 苹果的最大数量
    max_apples_count INT          NOT NULL DEFAULT 10,
    # 每个用户每天可以摘取的苹果数目
    limit_per_sprite INT          NOT NULL DEFAULT 1,
    constraint fk_tree_building FOREIGN KEY (id) REFERENCES building (id)
);

# 限制每个玩家每天只能摘取从每颗树上摘下一定数目苹果
CREATE TABLE apple_picking
(
    # 玩家id
    sprite    VARCHAR(255) NOT NULL,
    # 树的id
    tree      VARCHAR(255) NOT NULL,
    # 摘取的苹果数目
    count     INT          NOT NULL DEFAULT 0,
    # 下次可以摘取的时间
    pick_time DATETIME     NOT NULL,
    PRIMARY KEY (sprite, tree),
    CONSTRAINT fk_apple_picking_owner FOREIGN KEY (sprite) REFERENCES sprite (id),
    CONSTRAINT fk_apple_picking_tree FOREIGN KEY (tree) REFERENCES tree (id)
);

# 创建商店商品表
CREATE TABLE store_item_type
(
    # 商品类型
    item_type VARCHAR(255) NOT NULL,
    # 商店
    store     VARCHAR(255) NOT NULL,
    # 商品数量
    count     INT          NOT NULL DEFAULT 0,
    # 商品价格
    price     INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (item_type, store),
    CONSTRAINT fk_store_item_store FOREIGN KEY (store) REFERENCES building (id),
    CONSTRAINT fk_store_item_item FOREIGN KEY (item_type) REFERENCES item_type (id)
);

