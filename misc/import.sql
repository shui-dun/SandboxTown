-- 删除数据库
DROP DATABASE IF EXISTS sandbox_town;

-- 创建数据库
CREATE DATABASE sandbox_town;
USE sandbox_town;

-- 创建用户表
CREATE TABLE user (
  username VARCHAR(255) NOT NULL PRIMARY KEY,
  password VARCHAR(255) NOT NULL,
  salt VARCHAR(255) NOT NULL,
  ban_end_date DATETIME,
  cheat_count INT DEFAULT 0
);

INSERT INTO user (username, password, salt) VALUES ('haha', '3ff432d13d5060159f9daf745c6c0c414624159bce95b32437e6e4c59211a144', 'I+zxIDZF1PJ/G/LGQrwtgw==');
INSERT INTO user (username, password, salt) VALUES ('heihei', 'ed63891bacfd0861da88898ad002534f6d61058bce25c41e67b763a2d95f642a', 'ykzgDWYsa77gmD2bhMm41A==');
INSERT INTO user (username, password, salt) VALUES ('xixi', '38232ca0c66eb3f33cc55696233fbf905fd02ceaad4242f5cd41b97b272c55d8', 'KaSg9wFMopkEaU/cDY+Xvg==');

-- 创建用户权限表
CREATE TABLE user_role (
  username VARCHAR(255) NOT NULL,
  role VARCHAR(255) NOT NULL,
  PRIMARY KEY (username, role),
  CONSTRAINT fk_user_role_username FOREIGN KEY (username) REFERENCES user(username)
);


INSERT INTO user_role (username, role) VALUES ('haha', 'normal');
INSERT INTO user_role (username, role) VALUES ('heihei', 'admin');
INSERT INTO user_role (username, role) VALUES ('xixi', 'normal');
