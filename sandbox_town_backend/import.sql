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
    ban_end_date DATETIME,
    cheat_count  INT DEFAULT 0
);

# 密码都是123456
INSERT INTO user (username, password, salt)
VALUES ('user_haha', '3ff432d13d5060159f9daf745c6c0c414624159bce95b32437e6e4c59211a144', 'I+zxIDZF1PJ/G/LGQrwtgw==');
INSERT INTO user (username, password, salt)
VALUES ('user_heihei', 'ed63891bacfd0861da88898ad002534f6d61058bce25c41e67b763a2d95f642a', 'ykzgDWYsa77gmD2bhMm41A==');
INSERT INTO user (username, password, salt)
VALUES ('user_xixi', '38232ca0c66eb3f33cc55696233fbf905fd02ceaad4242f5cd41b97b272c55d8', 'KaSg9wFMopkEaU/cDY+Xvg==');

# 创建用户权限表
CREATE TABLE user_role
(
    username VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL,
    PRIMARY KEY (username, role),
    CONSTRAINT fk_user_role_username FOREIGN KEY (username) REFERENCES user (username)
);


INSERT INTO user_role (username, role)
VALUES ('user_haha', 'normal');
INSERT INTO user_role (username, role)
VALUES ('user_heihei', 'admin');
INSERT INTO user_role (username, role)
VALUES ('user_xixi', 'normal');


# 创建角色表，包含玩家、宠物、怪物等角色
CREATE TABLE `character`
(
    id      VARCHAR(255) NOT NULL PRIMARY KEY,
    # 主人
    owner   VARCHAR(255),
    money   INT          NOT NULL DEFAULT 0,
    exp     INT          NOT NULL DEFAULT 0,
    level   INT          NOT NULL DEFAULT 1,
    hunger  INT          NOT NULL DEFAULT 100,
    hp      INT          NOT NULL DEFAULT 100,
    attack  INT          NOT NULL DEFAULT 10,
    defense INT          NOT NULL DEFAULT 10,
    speed   INT          NOT NULL DEFAULT 10,
    x       INT          NOT NULL DEFAULT 0,
    y       INT          NOT NULL DEFAULT 0
);

INSERT INTO `character` (id, owner, money, exp, level, hunger, hp, attack, defense, speed, x, y)
VALUES ('user_xixi', null, 10, 0, 1, 100, 100, 10, 10, 10, 0, 0),
       ('user_haha', null, 10, 0, 1, 100, 100, 10, 10, 20, 100, 100),
       ('user_heihei', null, 10, 0, 1, 100, 100, 10, 10, 20, 200, 200);

# 创建物品表
CREATE TABLE item
(
    # id 用于标识物品，比如 wood, stone
    id           VARCHAR(255) NOT NULL PRIMARY KEY,
    # name 用于显示物品名称，比如 木头，石头
    name         VARCHAR(255) NOT NULL,
    description  VARCHAR(255) NOT NULL,
    # 注意这个价格只是参考价格，各个商店会有上下波动
    basic_price  INT          NOT NULL DEFAULT 0,
    # 基础稀有度
    basic_rarity INT          NOT NULL DEFAULT 0,
    # 是否能直接被使用
    usable       BOOLEAN      NOT NULL DEFAULT FALSE,
    # 增加金钱
    money_inc    INT          NOT NULL DEFAULT 0,
    # 增加经验值
    exp_inc      INT          NOT NULL DEFAULT 0,
    # 增加等级
    level_inc    INT          NOT NULL DEFAULT 0,
    # 增加饱腹值
    hunger_inc   INT          NOT NULL DEFAULT 0,
    # 增加生命值
    hp_inc       INT          NOT NULL DEFAULT 0,
    # 增加攻击力
    attack_inc   INT          NOT NULL DEFAULT 0,
    # 增加防御力
    defense_inc  INT          NOT NULL DEFAULT 0,
    # 增加速度
    speed_inc    INT          NOT NULL DEFAULT 0
);

INSERT INTO item (id, name, description, basic_price, basic_rarity, usable, money_inc, exp_inc, level_inc, hunger_inc,
                  hp_inc, attack_inc, defense_inc, speed_inc)
VALUES ('wood', '木头', '建筑的材料，也可处于烤火', 2, 100, FALSE, 0, 0, 0, 0, 0, 0, 0, 0),
       ('stone', '石头', '用于建造房屋和其他工具', 3, 70, FALSE, 0, 0, 0, 0, 0, 0, 0, 0),
       ('bread', '面包', '具有松软的质地和微甜的口感', 3, 70, TRUE, 0, 0, 0, 15, 0, 0, 0, 0),
       ('apple', '苹果', '禁忌和知识之果', 2, 80, TRUE, 0, 10, 0, 7, 0, 0, 0, 0);

# 创建角色物品表

CREATE TABLE character_item
(
    owner      VARCHAR(255) NOT NULL,
    item_id    VARCHAR(255) NOT NULL,
    item_count INT          NOT NULL DEFAULT 1,
    PRIMARY KEY (owner, item_id),
    CONSTRAINT fk_player_item_username FOREIGN KEY (owner) REFERENCES `character` (id),
    CONSTRAINT fk_player_item_item_name FOREIGN KEY (item_id) REFERENCES item (id)
);

INSERT INTO character_item
values ('user_xixi', 'wood', 3),
       ('user_xixi', 'stone', 1),
       ('user_xixi', 'bread', 2),
       ('user_xixi', 'apple', 3),
       ('user_haha', 'wood', 3),
       ('user_haha', 'stone', 1),
       ('user_haha', 'bread', 2),
       ('user_haha', 'apple', 3),
       ('user_heihei', 'wood', 3),
       ('user_heihei', 'stone', 1),
       ('user_heihei', 'bread', 2),
       ('user_heihei', 'apple', 3);

# 创建建筑类型表
CREATE TABLE building_type
(
    # 建筑的类型，例如store
    id          VARCHAR(255) NOT NULL PRIMARY KEY,
    # 描述信息
    description VARCHAR(255) NOT NULL,
    # 建筑的基础价格
    basic_price INT          NOT NULL DEFAULT 0,
    # 黑白图的路径
    image_path  VARCHAR(255) NOT NULL
);

INSERT INTO building_type (id, description, basic_price, image_path)
VALUES ('store', '买卖商品的场所', 200, 'static/bitmap/store.png'),
       ('tree', '可以伐木或摘苹果', 100, 'static/bitmap/tree.png');


# 创建建筑表
CREATE TABLE building
(
    # 建筑的id
    id             VARCHAR(255) NOT NULL PRIMARY KEY,
    # 建筑的类型
    type           VARCHAR(255) NOT NULL,
    # 建筑所在的地图名称
    map            VARCHAR(255) NOT NULL,
    # 建筑的等级（等级越高，收益越好，例如商店等级越高，商品数目越多，商品价格越低，物品价格由其基本价格和商店等价和随机数共同决定）
    level          INT          NOT NULL DEFAULT 1,
    # 建筑的拥有者
    owner          VARCHAR(255) NOT NULL,
    # 建筑左上角的x坐标（对于建筑，我们使用图像左上角的坐标，以方便寻路算法，但是对于玩家等，我们使用质心的坐标）
    originX        INT          NOT NULL,
    # 建筑左上角的y坐标
    originY        INT          NOT NULL,
    # 建筑的宽度
    display_width  INT          NOT NULL,
    # 建筑的高度
    display_height INT          NOT NULL,
    CONSTRAINT fk_building_type FOREIGN KEY (type) REFERENCES building_type (id),
    CONSTRAINT fk_building_owner FOREIGN KEY (owner) REFERENCES `character` (id)
);

INSERT INTO building (id, type, map, level, owner, originX, originY, display_width, display_height)
VALUES ('store_Pk86H7rTSm2XJdGoHFe-7A', 'store', '1', 1, 'user_xixi', 0, 0, 300, 300),
       ('tree_hjQLffrhQayNLVuty_poLg', 'tree', '1', 1, 'user_xixi', 200, 500, 400, 400);
