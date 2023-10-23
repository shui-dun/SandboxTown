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
    # ON DELETE CASCADE 表示级联删除，当删除user表中的一条记录时，user_role表中对应的记录也会被删除
    CONSTRAINT fk_user_role_username FOREIGN KEY (username) REFERENCES user (username) ON DELETE CASCADE
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

# 创建角色类型表
CREATE TABLE sprite_type
(
    # 角色类型
    type               VARCHAR(255) NOT NULL PRIMARY KEY,
    # 角色名称
    name               VARCHAR(255) NOT NULL,
    description        VARCHAR(255) NOT NULL,
    # 注意这个价格只是参考价格，各个商店会有上下波动
    basic_price        INT          NOT NULL DEFAULT 0,
    # 基础金钱
    basic_money        INT          NOT NULL DEFAULT 0,
    # 基础经验值
    basic_exp          INT          NOT NULL DEFAULT 0,
    # 基础等级
    basic_level        INT          NOT NULL DEFAULT 1,
    # 基础饱腹值
    basic_hunger       INT          NOT NULL DEFAULT 100,
    # 基础生命值
    basic_hp           INT          NOT NULL DEFAULT 100,
    # 基础攻击力
    basic_attack       INT          NOT NULL DEFAULT 10,
    # 基础防御力
    basic_defense      INT          NOT NULL DEFAULT 10,
    # 基础速度
    basic_speed        INT          NOT NULL DEFAULT 10,
    # 基础宽度
    basic_width        DOUBLE       NOT NULL DEFAULT 120,
    # 基础高度
    basic_height       DOUBLE       NOT NULL DEFAULT 120,
    # 基础视觉范围
    basic_vision_range INT          NOT NULL DEFAULT 1000,
    # 基础攻击范围
    basic_attack_range INT          NOT NULL DEFAULT 100,
    # 精灵宽度占图像宽度的比例
    width_ratio        DOUBLE       NOT NULL DEFAULT 1,
    # 精灵高度占图像高度的比例
    height_ratio       DOUBLE       NOT NULL DEFAULT 1
);

INSERT INTO sprite_type (type, name, description, basic_price, basic_money,
                         basic_exp, basic_level, basic_hunger, basic_hp,
                         basic_attack, basic_defense, basic_speed, basic_width, basic_height, basic_vision_range,
                         basic_attack_range, width_ratio, height_ratio)
VALUES ('USER', '玩家', '小镇居民', 0, 0, 0, 1, 100, 100, 10, 6, 10, 160, 160, 1000, 100, 0.5, 0.8),
       ('DOG', '狗狗', '可靠的护卫，忠诚而勇敢，像你的影子一样一直陪伴着你', 0, 0, 0, 1, 100, 100, 10, 5, 10, 180, 180,
        1000, 100, 0.35, 0.5),
       ('CAT', '猫咪', '常见的家养宠物，具有柔软的毛发和灵活的身体，喜爱捕鱼', 0, 0, 0, 1, 100, 100, 9, 5, 10, 160, 160,
        1000, 100, 0.35, 0.5),
       ('SPIDER', '蜘蛛', '八腿的恶棍，以其敏捷和毒液为武器', 0, 0, 0, 1, 100, 100, 12, 4, 8, 170, 170, 300, 100,
        0.5, 0.35);

# 创建角色表，包含玩家、宠物、怪物等角色
CREATE TABLE sprite
(
    id           VARCHAR(255) NOT NULL PRIMARY KEY,
    # 类型
    type         VARCHAR(255) NOT NULL,
    # 主人
    owner        VARCHAR(255),
    # 角色的各个属性都是基础值，不包括装备、效果等加成
    money        INT                   DEFAULT 0,
    exp          INT          NOT NULL DEFAULT 0,
    level        INT          NOT NULL DEFAULT 1,
    hunger       INT          NOT NULL DEFAULT 100,
    hp           INT          NOT NULL DEFAULT 100,
    attack       INT          NOT NULL DEFAULT 10,
    defense      INT          NOT NULL DEFAULT 10,
    speed        INT          NOT NULL DEFAULT 10,
    # 视觉范围
    vision_range INT          NOT NULL DEFAULT 1000,
    # 攻击范围
    attack_range INT          NOT NULL DEFAULT 100,
    x            DOUBLE       NOT NULL DEFAULT 0,
    y            DOUBLE       NOT NULL DEFAULT 0,
    width        DOUBLE       NOT NULL DEFAULT 120,
    height       DOUBLE       NOT NULL DEFAULT 120,
    # 所在地图名称
    map          VARCHAR(255) NOT NULL,
    CONSTRAINT fk_sprite_type FOREIGN KEY (type) REFERENCES sprite_type (type),
    CONSTRAINT fk_sprite_map FOREIGN KEY (map) REFERENCES game_map (id) ON DELETE CASCADE,
    CONSTRAINT fk_sprite_owner FOREIGN KEY (owner) REFERENCES sprite (id) ON DELETE CASCADE
);

INSERT INTO sprite (id, type, owner, money, exp, level, hunger, hp, attack, defense, speed, vision_range, attack_range,
                    x, y, map, width,
                    height)
VALUES ('USER_xixi', 'USER', null, 10, 0, 1, 100, 100, 10, 6, 10, 1000, 100, 300, 300, '1', 160, 160),
       ('USER_haha', 'USER', null, 10, 0, 1, 100, 100, 10, 6, 20, 1000, 100, 100, 100, '1', 160, 160),
       ('USER_heihei', 'USER', null, 10, 0, 1, 100, 100, 10, 6, 20, 1000, 100, 200, 200, '1', 160, 160),
       ('DOG_Vz5n_o-CQk-okcK5vQFRsA', 'DOG', 'USER_xixi', 0, 10, 2, 70, 40, 8, 6, 8, 1000, 100, 400, 300, '1', 150,
        150),
       ('DOG_q83jrKyCTtGm1QvywN48pw', 'DOG', 'USER_xixi', 0, 10, 2, 70, 40, 13, 6, 8, 1000, 100, 400, 400, '1', 250,
        250),
       ('CAT_iZUc8IiRTCOQXNjLNbQUFQ', 'CAT', 'USER_xixi', 0, 10, 2, 70, 40, 8, 6, 8, 1000, 100, 400, 500, '1', 150,
        150);

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
    # 耐久度（范围在0~100之间，耐久度越高寿命越长，-1代表无限耐久，同时也代表可堆叠）
    durability  INT          NOT NULL DEFAULT -1
);

INSERT INTO item_type (id, name, description, basic_price, rarity, durability)
VALUES ('WOOD', '木头', '建筑的材料，也可处于烤火', 5, 40, -1),
       ('STONE', '石头', '用于建造房屋和其他工具', 8, 30, -1),
       ('BREAD', '面包', '具有松软的质地和微甜的口感', 30, 7, -1),
       ('APPLE', '苹果', '禁忌和知识之果', 5, 40, -1),
       ('TREASURE_CHEST', '宝箱', '打开宝箱，获得随机物品', 20, 7, -1),
       ('FLYING_BOOTS', '飞翔靴', '让风成为你最忠实的伙伴', 100, 4, 40),
       ('INVISIBLE_CAP', '隐身帽', '藏匿无影', 280, 3, 50),
       ('LEATHER_CHEST_ARMOR', '皮质盔甲', '提供基础的防御', 40, 16, 25),
       ('SAW', '锯子', '伐木的好帮手', 30, 40, 30),
       ('STICK', '木棍', '基础的攻击武器', 22, 40, 40),
       ('BONE', '骨头', '狗狗的最爱', 20, 10, -1),
       ('IRON_HELMET', '铁质头盔', '坚固耐用,能够抵挡强力击打', 70, 10, 40),
       ('PHOENIX_FEATHER', '凤凰之羽', '凤凰的羽毛具有无比强大的火焰和治愈效果', 100, 4, -1),
       ('HOLY_GRAIL', '圣杯', '使疲惫的灵魂和肉体重获新生', 240, 2, 100),
       ('FLAME_LEGGINGS', '火焰护腿', '每一步都踏着烈焰,将敌人化为灰烬', 70, 12, 40);

# 杀死精灵时的属性奖励
# 该表按理来说可以直接放在精灵类型表中，但这里我将其和sprite_type分开：
# - 该表只有在杀死精灵后才会被查询，将它们分成两个表可以使数据模型更加模块化，更容易理解。
# - 该表查询频率相对较低，将其与"精灵类型"表分开可以减少查询"精灵类型"时的I/O负担（但由于该表字段较少，这个优化效果几乎没有）
create table victory_attribute_reward
(
    sprite_type varchar(255) not null primary key,
    money_inc   int          not null,
    exp_inc     int          not null,
    foreign key (sprite_type) references sprite_type (type)
);

insert into victory_attribute_reward(sprite_type, money_inc, exp_inc)
values ('DOG', 15, 15),
       ('SPIDER', 20, 20);

# 杀死精灵时的物品奖励
create table victory_item_reward
(
    sprite_type varchar(255) not null,
    item_type   varchar(255) not null,
    # 最小物品奖励数目（可以为负）
    min_count   int          not null,
    max_count   int          not null,
    primary key (sprite_type, item_type),
    foreign key (sprite_type) references sprite_type (type),
    foreign key (item_type) references item_type (id)
);

insert into victory_item_reward(sprite_type, item_type, min_count, max_count)
values ('DOG', 'BONE', -7, 3),
       ('SPIDER', 'BREAD', -7, 2),
       ('SPIDER', 'STICK', -20, 2);

# 喂食表
create table feed
(
    sprite_type varchar(255) not null,
    item_type   varchar(255) not null,
    # 驯服概率（0-1）
    tame_prob   double       not null,
    # 经验提升
    exp_inc     int          not null,
    # 饱腹值提升
    hunger_inc  int          not null,
    primary key (sprite_type, item_type),
    foreign key (sprite_type) references sprite_type (type),
    foreign key (item_type) references item_type (id)
);

insert into feed(sprite_type, item_type, tame_prob, exp_inc, hunger_inc)
values ('DOG', 'BONE', 0.28, 15, 50);

# 物品标签表
create table item_type_label
(
    item_type varchar(255) not null,
    # 物品的标签包含FOOD（可食用）、USABLE（用品）、WEAPON（武器）、HELMET（头盔）, CHEST（胸甲）, LEG（腿甲）, BOOTS（鞋）
    label     varchar(255) not null,
    primary key (item_type, label),
    foreign key (item_type) references item_type (id)
);

insert into item_type_label
values ('BREAD', 'FOOD'),
       ('APPLE', 'FOOD'),
       ('TREASURE_CHEST', 'USABLE'),
       ('FLYING_BOOTS', 'BOOTS'),
       ('INVISIBLE_CAP', 'HELMET'),
       ('LEATHER_CHEST_ARMOR', 'CHEST'),
       ('SAW', 'WEAPON'),
       ('STICK', 'WEAPON'),
       ('IRON_HELMET', 'HELMET'),
       ('PHOENIX_FEATHER', 'USABLE'),
       ('FLAME_LEGGINGS', 'LEG');

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
       ('LIFE', '生命', '丰盛的生命，持续治愈伤口'),
       ('BURN', '烧伤', '烧伤，收到持续性的伤害'),
       ('FLAME_BODY', '火焰护体', '以熊熊烈火包裹自身，任何靠近进行攻击的生物都将被灼伤');

# 角色的效果列表
# 只包含使用物品带来的效果以及其过期时间，装备的效果自己去查
create table sprite_effect
(
    sprite   VARCHAR(255) NOT NULL,
    effect   VARCHAR(255) NOT NULL,
    # 效果的总持续时间（单位是秒，而不是毫秒，-1表示永久）
    duration INT          NOT NULL DEFAULT 0,
    # 效果的过期时间（1970年1月1日至今的毫秒数，-1表示永久）
    expire   BIGINT       NOT NULL,
    primary key (sprite, effect),
    CONSTRAINT fk_sprite_effect_sprite FOREIGN KEY (sprite) REFERENCES sprite (id) ON DELETE CASCADE,
    CONSTRAINT fk_sprite_effect_effect FOREIGN KEY (effect) REFERENCES effect (id)
);

# 装备、使用、或手持该物品后对角色各个属性的增益值
# 注意：这些增益值指的是等级为1的物品带来的增益值，等级越高，增益值越高
create table item_type_attribute
(
    item_type        varchar(255) not null,
    # 装备（EQUIP）、使用（USE）、或手持（HANDHELD）
    # 装备指在装备栏放置HELMET（头盔）, CHEST（胸甲）, LEG（腿甲）, BOOTS（鞋）
    # 使用指使用FOOD（食品）、USABLE（用品）
    # 手持指手持物品
    operation        varchar(255) not null,
    # 增加金钱
    money_inc        INT          NOT NULL DEFAULT 0,
    # 增加经验值
    exp_inc          INT          NOT NULL DEFAULT 0,
    # 增加等级
    level_inc        INT          NOT NULL DEFAULT 0,
    # 增加饱腹值
    hunger_inc       INT          NOT NULL DEFAULT 0,
    # 增加生命值
    hp_inc           INT          NOT NULL DEFAULT 0,
    # 增加攻击力
    attack_inc       INT          NOT NULL DEFAULT 0,
    # 增加防御力
    defense_inc      INT          NOT NULL DEFAULT 0,
    # 增加速度
    speed_inc        INT          NOT NULL DEFAULT 0,
    # 增加视觉范围
    vision_range_inc INT          NOT NULL DEFAULT 0,
    # 增加攻击范围
    attack_range_inc INT          NOT NULL DEFAULT 0,
    primary key (item_type, operation),
    foreign key (item_type) references item_type (id)
);

insert into item_type_attribute(item_type, operation, money_inc, exp_inc, level_inc, hunger_inc, hp_inc, attack_inc,
                                defense_inc, speed_inc, vision_range_inc, attack_range_inc)
values ('BREAD', 'USE', 0, 0, 0, 10, 0, 0, 0, 0, 0, 0),
       ('APPLE', 'USE', 0, 4, 0, 4, 0, 0, 0, 0, 0, 0),
       ('LEATHER_CHEST_ARMOR', 'EQUIP', 0, 0, 0, 0, 0, 0, 3, 0, 0, 0),
       ('SAW', 'HANDHELD', 0, 0, 0, 0, 0, 15, 0, 0, 0, 0),
       ('STICK', 'HANDHELD', 0, 0, 0, 0, 0, 5, 0, 0, 0, 0),
       ('BONE', 'HANDHELD', 0, 0, 0, 0, 0, 7, 0, 0, 0, 0),
       ('FLYING_BOOTS', 'EQUIP', 0, 0, 0, 0, 0, 0, 0, 5, 0, 0),
       ('IRON_HELMET', 'EQUIP', 0, 0, 0, 0, 0, 0, 7, 0, 0, 0),
       ('HOLY_GRAIL', 'HANDHELD', 0, 0, 0, 5, 0, 5, 5, 5, 100, 5),
       ('FLAME_LEGGINGS', 'EQUIP', 0, 0, 0, 0, 0, 3, 5, 0, 0, 0);


# 装备物品后对对角色带来的特殊效果
# 注意：这些效果的持续时间指的是等级为1的物品的效果持续时间，等级越高，持续时间越长
create table item_type_effect
(
    item_type varchar(255) not null,
    # 进行什么操作，装备（EQUIP）、使用（USE）、或手持（HANDHELD）
    operation varchar(255) not null,
    effect    varchar(255) not null,
    # 持续时间（秒）
    # -1 表示永久
    duration  INT          NOT NULL DEFAULT 0,
    primary key (item_type, operation, effect),
    foreign key (item_type) references item_type (id),
    foreign key (effect) references effect (id)
);

insert into item_type_effect(item_type, operation, effect, duration)
values ('PHOENIX_FEATHER', 'USE', 'LIFE', 90),
       ('PHOENIX_FEATHER', 'USE', 'FLAME_BODY', 60),
       ('HOLY_GRAIL', 'HANDHELD', 'LIFE', -1),
       ('FLAME_LEGGINGS', 'EQUIP', 'FLAME_BODY', -1);


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
    FOREIGN KEY (owner) REFERENCES sprite (id) ON DELETE CASCADE,
    FOREIGN KEY (item_type) REFERENCES item_type (id)
);

insert into item(id, owner, item_type, item_count, life, level, position)
values ('BREAD_jhddfddffiu', 'USER_haha', 'BREAD', 1, 100, 1, 'BACKPACK'),
       ('APPLE_hdjfdjedfio', 'USER_haha', 'APPLE', 5, 100, 1, 'BACKPACK'),
       ('TREASURE_CHEST_ixdiudfdfde', 'USER_haha', 'TREASURE_CHEST', 1, 100, 1, 'BACKPACK'),
       ('SAW_jhdfdfddffiu', 'USER_haha', 'SAW', 1, 100, 1, 'BACKPACK'),
       ('SAW_sdaajhdfdfddffiu', 'USER_haha', 'SAW', 1, 100, 1, 'HANDHELD'),
       ('LEATHER_CHEST_ARMOR_saeeiffkdfdlf', 'USER_haha', 'LEATHER_CHEST_ARMOR', 1, 100, 1, 'BACKPACK'),
       ('LEATHER_CHEST_ARMOR_dkfjeiffkdfdlf', 'USER_haha', 'LEATHER_CHEST_ARMOR', 1, 100, 1, 'CHEST'),
       ('FLYING_BOOTS_dkfjeidfeffkdlf', 'USER_haha', 'FLYING_BOOTS', 1, 100, 1, 'BOOTS'),
       ('BONE_djkfisefkeddgsldfldifdj', 'USER_haha', 'BONE', 24, 100, 1, 'BACKPACK'),
       ('BREAD_jhdfiu', 'USER_xixi', 'BREAD', 1, 100, 1, 'BACKPACK'),
       ('APPLE_hdjfdjeio', 'USER_xixi', 'APPLE', 5, 100, 1, 'BACKPACK'),
       ('TREASURE_CHEST_ixdiue', 'USER_xixi', 'TREASURE_CHEST', 1, 100, 1, 'BACKPACK'),
       ('SAW_jhdfiu', 'USER_xixi', 'SAW', 1, 100, 1, 'BACKPACK'),
       ('SAW_sdaajhdfiu', 'USER_xixi', 'SAW', 1, 100, 1, 'HANDHELD'),
       ('LEATHER_CHEST_ARMOR_saeeiffkdlf', 'USER_xixi', 'LEATHER_CHEST_ARMOR', 1, 100, 1, 'BACKPACK'),
       ('LEATHER_CHEST_ARMOR_dkfjeiffkdlf', 'USER_xixi', 'LEATHER_CHEST_ARMOR', 1, 100, 1, 'CHEST'),
       ('FLYING_BOOTS_dkfjeiffkdlf', 'USER_xixi', 'FLYING_BOOTS', 1, 100, 1, 'BOOTS'),
       ('BONE_djkfisefksldfldifdj', 'USER_xixi', 'BONE', 24, 100, 1, 'BACKPACK'),
       ('IRON_HELMET_dkdcclkdfded', 'USER_xixi', 'IRON_HELMET', 1, 100, 1, 'BACKPACK'),
       ('PHOENIX_FEATHER_dkdcclkdfded', 'USER_xixi', 'PHOENIX_FEATHER', 10, 100, 1, 'BACKPACK'),
       ('FLAME_LEGGINGS_dkdcclkdfded', 'USER_xixi', 'FLAME_LEGGINGS', 1, 100, 1, 'BACKPACK'),
       ('HOLY_GRAIL_dkdcclkdfded', 'USER_xixi', 'HOLY_GRAIL', 1, 100, 1, 'BACKPACK');


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
    basic_width  DOUBLE       NOT NULL DEFAULT 0,
    # 基础高度（真实高度会在此基础上波动）
    basic_height DOUBLE       NOT NULL DEFAULT 0,
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
    origin_x DOUBLE       NOT NULL,
    # 建筑左上角的y坐标
    origin_y DOUBLE       NOT NULL,
    # 建筑的宽度
    width    DOUBLE       NOT NULL,
    # 建筑的高度
    height   DOUBLE       NOT NULL,
    CONSTRAINT fk_building_type FOREIGN KEY (type) REFERENCES building_type (id),
    CONSTRAINT fk_building_owner FOREIGN KEY (owner) REFERENCES sprite (id),
    CONSTRAINT fk_building_map FOREIGN KEY (map) REFERENCES game_map (id) ON DELETE CASCADE
);

# 精灵刷新表
create table sprite_refresh
(
    sprite_type   varchar(255) not null,
    # 刷新在哪种建筑附近
    building_type varchar(255),
    #  最小刷新数目（这个数可以是负数）
    min_count     int          not null,
    # 最大刷新数目
    max_count     int          not null,
    # 刷新时间（白天、黄昏、晚上、匿名），其中，晚上被刷新的精灵自动被视为夜行动物
    refresh_time  varchar(255) not null,
    primary key (sprite_type, building_type, refresh_time),
    foreign key (sprite_type) references sprite_type (type),
    foreign key (building_type) references building_type (id)
);

insert into sprite_refresh(sprite_type, building_type, min_count, max_count, refresh_time)
values ('DOG', 'STORE', -8, 4, 'DAWN'),
       ('DOG', 'STORE', -6, 2, 'DUSK'),
       ('SPIDER', 'TREE', -18, 3, 'NIGHT');

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
    constraint fk_tree_building FOREIGN KEY (id) REFERENCES building (id) ON DELETE CASCADE
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
    CONSTRAINT fk_apple_picking_owner FOREIGN KEY (sprite) REFERENCES sprite (id) ON DELETE CASCADE,
    CONSTRAINT fk_apple_picking_tree FOREIGN KEY (tree) REFERENCES tree (id) ON DELETE CASCADE
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
    CONSTRAINT fk_store_item_store FOREIGN KEY (store) REFERENCES building (id) ON DELETE CASCADE,
    CONSTRAINT fk_store_item_item FOREIGN KEY (item_type) REFERENCES item_type (id)
);

