import Phaser from "phaser";
import mixin from "@/js/mixin.js";
import ws from "./websocket";
import emitter from "./mitt";

class MainScene extends Phaser.Scene {
    constructor() {
        super();

        this.key = 'main';

        // 是否加载完成
        this.isLoaded = false;

        // 设置id->gameObject的映射
        this.id2gameObject = {};

        // 设置id->sprite的映射
        this.id2spriteInfo = {};

        // 地图信息
        this.gameMap = null;

        // 当前用户的用户名
        this.myUsername = null;

        // 碰撞形状
        this.collapseShapes = null;

        // 点击形状
        this.clickShapes = null;

        // 保存所有计时器
        this.timerList = [];

        // 角色->补间动画
        this.id2tween = {};

        // 精灵对象->HP变化信息
        // 之所以这里要用Map，是因为{}中只能用String作为key
        this.sprite2hpMsg = new Map();

        // 精灵对象->精灵名称文本对象
        this.nameMsg2sprite = new Map();

    }

    // 更新文本信息的位置
    updateTextMsgPosition() {
        for (let [sprite, textMsg] of this.sprite2hpMsg.entries()) {
            if (!sprite || !textMsg || !sprite.body) {
                continue;
            }
            textMsg.x = sprite.x;
            textMsg.y = sprite.y - sprite.displayHeight / 2;
            textMsg.setDepth(textMsg.y + 1000);
        }

        for (let [sprite, textMsg] of this.nameMsg2sprite.entries()) {
            if (!sprite || !textMsg || !sprite.body) {
                continue;
            }
            textMsg.x = sprite.x;
            textMsg.y = sprite.y + sprite.displayHeight / 2 + 15;
            textMsg.setDepth(textMsg.y);
        }
    }

    // 设置物体的层数，层数越高，显示越靠前
    setDepth(gameObject) {
        // shape中心的y坐标
        gameObject.setDepth(gameObject.y);
    }

    // 将图像左上角坐标转化为物体质心坐标
    convertToCenter(gameObject, x, y) {
        let massOffsetX = gameObject.body.centerOffset.x;
        let massOffsetY = gameObject.body.centerOffset.y;
        let massX = x + massOffsetX * gameObject.body.scale.x;
        let massY = y + massOffsetY * gameObject.body.scale.y;
        return { x: massX, y: massY };
    }

    // 创建角色
    createSprite(sprite) {
        // 如果角色已经存在，则不再创建
        if (this.id2spriteInfo[sprite.id]) {
            return;
        }
        // 将其加入id2sprite
        this.id2spriteInfo[sprite.id] = sprite;
        // 创建角色
        let spriteSprite = this.matter.add.sprite(0, 0, sprite.type, null, { shape: this.collapseShapes[sprite.type] });
        // 设置角色位置
        spriteSprite.setDisplaySize(sprite.width, sprite.height);
        // 设置位置
        spriteSprite.setPosition(sprite.x, sprite.y);
        // 设置角色层级
        this.setDepth(spriteSprite);
        // 禁止旋转
        spriteSprite.setFixedRotation();
        // 显示玩家的名称
        let name = sprite.id;
        if (name.startsWith("USER_")) {
            name = name.split("_", 2)[1];
            let nameMsg = this.add.text(0, 0, name, { fontFamily: 'Consolas', fontSize: 22, color: '#000000' });
            nameMsg.setOrigin(0.5, 1);
            this.nameMsg2sprite.set(spriteSprite, nameMsg);
        }
        // 设置点击角色的事件
        spriteSprite.setInteractive({ hitArea: new Phaser.Geom.Polygon(this.clickShapes[sprite.type]), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
        spriteSprite.on('pointerdown', (pointer, _localX, _localY, event) => {
            // 鼠标左键点击，与精灵进行交互
            if (pointer.button === 0) {
                // 如果是自己，则不进行交互
                if (sprite.id === this.myUsername) {
                    return;
                }
                const worldPoint = this.cameras.main.getWorldPoint(pointer.x, pointer.y);
                const x = worldPoint.x;
                const y = worldPoint.y;
                // 发送移动请求
                ws().send(JSON.stringify({
                    "type": "MOVE",
                    "data": {
                        "x0": this.id2gameObject[this.myUsername].x.toFixed(2),
                        "y0": this.id2gameObject[this.myUsername].y.toFixed(2),
                        "x1": x.toFixed(2),
                        "y1": y.toFixed(2),
                        "destBuildingId": null,
                        "destSpriteId": sprite.id,
                    }
                }));
            } else if (pointer.button === 2) { // 鼠标右键点击
                this.game.events.emit('forward', { name: 'showSpritePanel', data: sprite.id });
            }
            // 防止右键点击时浏览器的默认行为（例如显示上下文菜单）
            this.input.mouse.disableContextMenu();
            // 阻止事件冒泡
            event.stopPropagation();
        });
        // 放置到字典中
        this.id2gameObject[sprite.id] = spriteSprite;
    }

    // 根据id获得游戏对象（不存在时会自动创建）
    async getGameObjectById(id) {
        // 如果id2gameObject中不存在该id，说明需要手动从后端获得
        if (this.id2gameObject[id] == null) {
            let response = await mixin.myGET(`/rest/sprite/list/${id}`);
            this.createSprite(response);
        }
        return this.id2gameObject[id];
    }

    // 根据id获得精灵信息（不存在时会自动创建）
    async getSpriteInfoById(id) {
        // 如果id2gameObject中不存在该id，说明需要手动从后端获得
        if (this.id2spriteInfo[id] == null) {
            let response = await mixin.myGET(`/rest/sprite/list/${id}`);
            this.createSprite(response);
        }
        return this.id2spriteInfo[id];
    }

    preload() {
        this.load.image("USER", require("@/assets/img/USER.png"));
        this.load.image("DOG", require("@/assets/img/DOG.png"));
        this.load.image("SPIDER", require("@/assets/img/SPIDER.png"));
        this.load.image("CAT", require("@/assets/img/CAT.png"));
        this.load.image("STORE", require("@/assets/img/STORE.png"));
        this.load.image("TREE", require("@/assets/img/TREE.png"));

        // 围墙
        this.load.image("WALL", require("@/assets/img/WALL.png"));

        // 加载纹理图片
        this.load.spritesheet("TILES", require("@/assets/img/TILES.png"), { frameWidth: 128, frameHeight: 128, endFrame: 11 });

        this.load.json('collapseShapes', require("@/assets/json/collapseShapes.json"));
        this.load.json('clickShapes', require("@/assets/json/clickShapes.json"));

    }
    async create() {
        // 防止右键点击时浏览器的默认行为（例如显示上下文菜单）
        this.input.mouse.disableContextMenu();

        // 得到形状
        this.collapseShapes = this.cache.json.get('collapseShapes');
        this.clickShapes = this.cache.json.get('clickShapes');

        // 得到地图信息
        this.gameMap = await mixin.myGET('/rest/gamemap/getGameMap');

        // 得到自己以及自己宠物的信息
        let myAndMyPetInfo = await mixin.myGET('/rest/sprite/myAndMyPetInfo');
        // 得到当前用户的用户名
        this.myUsername = myAndMyPetInfo.me.id;

        // 得到当前在线的角色列表
        let spriteList = await mixin.myGET('/rest/sprite/listAllOnline');
        // 将自己和自己的宠物加入角色列表
        spriteList.push(myAndMyPetInfo.me);
        spriteList.push(...myAndMyPetInfo.myPets);

        // 创建所有角色
        for (let i = 0; i < spriteList.length; i++) {
            let sprite = spriteList[i];
            // 创建角色
            this.createSprite(sprite);
        }

        // 得到建筑列表
        let buildingList = await mixin.myGET('/rest/building/getAllBuildings');

        // 创建建筑
        for (let i = 0; i < buildingList.length; i++) {
            let building = buildingList[i];
            // 创建建筑物
            let buildingSprite = this.matter.add.sprite(0, 0, building.type, null, { isStatic: true, shape: this.collapseShapes[building.type] });
            // 设置建筑物大小和位置
            buildingSprite.setDisplaySize(building.width, building.height);
            let axis = this.convertToCenter(buildingSprite, building.originX, building.originY);
            buildingSprite.setPosition(axis.x, axis.y);
            // 设置建筑物层级
            this.setDepth(buildingSprite);
            // 设置点击建筑物的事件
            buildingSprite.setInteractive({ hitArea: new Phaser.Geom.Polygon(this.clickShapes[building.type]), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
            buildingSprite.on('pointerdown', (pointer, _localX, _localY, event) => {
                const worldPoint = this.cameras.main.getWorldPoint(pointer.x, pointer.y);
                const x = worldPoint.x;
                const y = worldPoint.y;
                // 发送移动请求
                ws().send(JSON.stringify({
                    "type": "MOVE",
                    "data": {
                        "x0": this.id2gameObject[this.myUsername].x.toFixed(2),
                        "y0": this.id2gameObject[this.myUsername].y.toFixed(2),
                        "x1": x.toFixed(2),
                        "y1": y.toFixed(2),
                        "destBuildingId": building.id,
                        "destSpriteId": null,
                    }
                }));
                // 阻止事件冒泡
                event.stopPropagation();

            });
        }

        // 获得登录奖励
        let loginReward = await mixin.myPOST('/rest/user/enterGameToReceiveReward');
        if (loginReward != 0) {
            mixin.fadeInfoShow('登录奖励: ' + loginReward + '金币💰');
        }

        // 设置地图大小
        this.matter.world.setBounds(0, 0, this.gameMap.width, this.gameMap.height);

        // 相机设置
        this.cameras.main.setBackgroundColor('#c1d275');
        this.cameras.main.setBounds(0, 0, this.gameMap.width, this.gameMap.height);

        // 遍历每个区域，创建背景纹理
        let textureLen = 75;
        for (let i = 0; i < this.gameMap.width / textureLen; i++) {
            for (let j = 0; j < this.gameMap.height / textureLen; j++) {
                // 一定概率创建纹理
                if (Math.random() > 0.05) {
                    continue;
                }
                let randomNum1 = Math.floor(Math.random() * 21) - 10;
                let randomNum2 = Math.floor(Math.random() * 21) - 10;
                const texture = this.add.sprite(i * textureLen + randomNum1, j * textureLen + randomNum2, 'TILES', Math.floor(Math.random() * 12));
                texture.setDisplaySize(textureLen, textureLen);
            }
        }

        // 创建围墙
        let pixelsPerGrid = 30;
        for (let x = 0; x < this.gameMap.data.length; ++x) {
            for (let y = 0; y < this.gameMap.data[0].length; ++y) {
                if (this.gameMap.data[x][y] == 1) {
                    const texture = this.matter.add.sprite(x * pixelsPerGrid, y * pixelsPerGrid, 'WALL', null, { isStatic: true, shape: this.collapseShapes["WALL"] })
                    texture.setDisplaySize(pixelsPerGrid, pixelsPerGrid);
                }
            }
        }

        // 相机跟随自己
        this.cameras.main.startFollow(this.id2gameObject[this.myUsername]);

        // 由于精灵被推动时，或是播放补间动画tween时，它的物理引擎不会更新其速度，速度都是0，因此在找到方法前，只同步位置，不同步速度
        // 每一段时间向服务器发送一次角色位置信息
        // 只发送自己、主人是自己、公共NPC（例如蜘蛛）的角色的坐标信息
        // 记录上一次发送的位置
        let lastAxisMap = {}
        this.timerList.push(setInterval(() => {
            // 如果连接未建立，就不发送
            if (ws().readyState !== 1) {
                return;
            }
            // 以一定概率切断链接（用于测试）
            // if (Math.random() > 0.98) {
            //     console.log("active Connection closed.");
            //     ws().close();
            //     return;
            // }
            let timestamp = new Date().getTime();
            // 遍历所有角色
            for (let id in this.id2spriteInfo) {
                // 如果角色是自己、主人是自己、公共NPC（例如蜘蛛）
                // TODO：这个逻辑应该被修改以支持级联主仆关系
                // TODO：后端owner的变化没有同步到前端
                if (id === this.myUsername ||
                    this.id2spriteInfo[id].owner === this.myUsername ||
                    (this.id2spriteInfo[id].owner == null && this.id2spriteInfo[id].type !== "USER")) {
                    // 如果上一次发送的位置和当前位置不同
                    if (lastAxisMap[id] == null ||
                        lastAxisMap[id].x !== this.id2gameObject[id].x ||
                        lastAxisMap[id].y !== this.id2gameObject[id].y) {
                        // 发送坐标信息
                        ws().send(JSON.stringify({
                            "type": "COORDINATE",
                            "data": {
                                "id": id,
                                "x": this.id2gameObject[id].x.toFixed(2),
                                "y": this.id2gameObject[id].y.toFixed(2),
                                "time": timestamp,
                                "vx": this.id2gameObject[id].body.velocity.x.toFixed(2),
                                "vy": this.id2gameObject[id].body.velocity.y.toFixed(2),
                            }
                        }));
                        // 更新上一次发送的位置
                        lastAxisMap[id] = {
                            "x": this.id2gameObject[id].x,
                            "y": this.id2gameObject[id].y,
                        }
                    }
                }
            }
        }, 50));


        // 碰撞检测
        let lastCollisionTime = 0;
        this.matter.world.on('collisionstart', (event) => {
            const now = Date.now();
            var pairs = event.pairs;
            var pair = pairs[0];
            var item1 = pair.bodyA.gameObject;
            var item2 = pair.bodyB.gameObject;
            if (item1 === null || item2 === null) {
                return;
            }
            // 如果是玩家与商店碰撞
            if (item1 === this.id2gameObject[this.myUsername] && item2.body.label === 'STORE'
                || item1.body.label === 'STORE' && item2 === this.id2gameObject[this.myUsername]) {
                if (now - lastCollisionTime < 1000) {
                    return;
                }
            }
            lastCollisionTime = now;
        });

        // 设置键盘输入监听
        this.cursors = this.input.keyboard.createCursorKeys();

        // 添加点击事件
        this.input.on('pointerdown', (pointer) => {
            const worldPoint = this.cameras.main.getWorldPoint(pointer.x, pointer.y);
            const x = worldPoint.x;
            const y = worldPoint.y;
            // 发送移动请求
            ws().send(JSON.stringify({
                "type": "MOVE",
                "data": {
                    "x0": this.id2gameObject[this.myUsername].x.toFixed(2),
                    "y0": this.id2gameObject[this.myUsername].y.toFixed(2),
                    "x1": x.toFixed(2),
                    "y1": y.toFixed(2),
                    "destBuildingId": null,
                    "destSpriteId": null,
                }
            }));
            // 防止右键点击时浏览器的默认行为（例如显示上下文菜单）
            this.input.mouse.disableContextMenu();
        });

        // 创建小地图
        // this.minimap = this.cameras.add(0, 0, 300, 150).setZoom(0.05).setName('mini');
        // this.minimap.setBackgroundColor('c1d275');
        // this.minimap.startFollow(id2gameObject[this.myUsername]);

        // 移动事件
        emitter.on('MOVE', async (data) => {
            // 移动事件的发起者
            let initatorSprite = await this.getSpriteInfoById(data.id);
            // 物品
            let initatorGameObject = await this.getGameObjectById(data.id);
            // 速度
            let speed = data.speed;
            // 路径
            let originPath = data.path;
            // 终点建筑id
            let destBuildingId = data.destBuildingId;
            // 终点精灵
            let destSpriteId = data.destSpriteId;
            let destSprite = null;
            if (destSpriteId != null) {
                destSprite = await this.getSpriteInfoById(destSpriteId);
            }
            // 目的地的到达事件
            let arriveEvent = () => {
                // 判断精灵是否是指定精灵或者其宠物
                let isOrIsOwneredby = (sprite, id) => {
                    return sprite.id == id || sprite.owner == id;
                }
                // 判断精灵是否是玩家或者玩家的宠物
                let isOrIsOwneredbyUser = (sprite) => {
                    return sprite.id.startsWith("USER") || (sprite.owner != null && sprite.owner.startsWith("USER"));
                }
                if (destBuildingId != null) {
                    // 当目标是建筑时，只有当发起者是自己或自己的宠物，才会触发到达事件
                    if (!isOrIsOwneredby(initatorSprite, this.myUsername)) {
                        return;
                    }
                }
                // 当目标是精灵时，只有当目标精灵是自己或自己的宠物，
                // 或者发起者是自己或自己的宠物同时目标精灵不是玩家并且主人也不是玩家，才会触发到达事件
                // 这是为了避免重复触发到达事件
                if (destSprite != null) {
                    if (!(
                        isOrIsOwneredby(destSprite, this.myUsername)
                        || (isOrIsOwneredby(initatorSprite, this.myUsername) && !isOrIsOwneredbyUser(destSprite))
                    )) {
                        return;
                    }
                }
                if (destBuildingId != null) {
                    let type = destBuildingId.split("_", 2)[0];
                    let targetID = destBuildingId;
                    if (type === 'TREE') {
                        emitter.emit('TREE_ARRIVE', { "initator": this.myUsername, "target": targetID });
                    } else if (type == 'STORE') {
                        this.game.events.emit('forward', { name: 'showStore', data: targetID });
                    }
                } else if (destSprite != null) {
                    // 发起交互事件
                    ws().send(JSON.stringify({
                        "type": "INTERACT",
                        "data": {
                            "source": initatorSprite.id,
                            "target": destSprite.id,
                        }
                    }));
                }
            };
            // 如果不存在路径，就直接到达终点
            if (originPath == null) {
                arriveEvent();
                return;
            }
            // 创建补间动画
            const path = new Phaser.Curves.Path(originPath[0], originPath[1]);
            let lastPos = originPath.length;
            // 如果路径长度为0，就直接到达终点
            if (lastPos <= 2) {
                arriveEvent();
                return;
            }
            for (let i = 2; i < lastPos; i += 2) {
                path.lineTo(originPath[i], originPath[i + 1]);
            }
            let tweenProgress = { value: 0 };
            if (this.id2tween[data.id] != null) {
                // 如果上一个补间动画还没结束，就停止上一个补间动画
                this.id2tween[data.id].stop();
            }
            let tween = this.tweens.add({
                targets: tweenProgress,
                value: 1,
                duration: 18 * path.getLength() / speed,
                ease: 'Linear',
                repeat: 0,
                onUpdate: () => {
                    try {
                        const point = path.getPoint(tweenProgress.value);
                        // 这个地方经常抛出异常，因为在玩家移动的过程中，玩家可能会下线，导致玩家被删除，但是补间动画还在继续，因此报错，因此要用try-catch包裹
                        initatorGameObject.setPosition(point.x, point.y);
                    } catch (error) {
                        console.log(error);
                    }
                },
                onComplete: () => {
                    this.id2tween[data.id] = null;
                    if (tween.isStopped) {
                        return;
                    }
                    arriveEvent();
                }
            });
            this.id2tween[data.id] = tween;
        });

        // 坐标通知事件
        emitter.on('COORDINATE', async (data) => {
            // 如果坐标通知带有速度，说明该角色在直接地移动，而非通过补间动画在移动（因为补间动画时速度为0）
            // 因此要停止补间动画
            if (data.vx != 0 || data.vy != 0) {
                if (this.id2tween[data.id] != null) {
                    this.id2tween[data.id].stop();
                }
            }
            // 游戏对象
            let gameObject = await this.getGameObjectById(data.id);
            // 更新其坐标
            gameObject.setPosition(data.x, data.y);
            // 更新速度
            this.matter.body.setVelocity(gameObject.body, { x: data.vx, y: data.vy });
        });

        // 下线通知事件
        // TODO: 如果下线消息丢失了该怎么办？
        emitter.on('OFFLINE', async (data) => {
            for (let spriteId of data.ids) {
                let gameObject = this.id2gameObject[spriteId];
                let nameMsg = this.nameMsg2sprite.get(gameObject);
                let tween = this.id2tween[spriteId];
                if (nameMsg) {
                    this.nameMsg2sprite.delete(gameObject);
                    nameMsg.destroy();
                }
                if (tween) {
                    tween.stop();
                    delete this.id2tween[spriteId];
                }

                delete this.id2gameObject[spriteId];
                gameObject.destroy();
                delete this.id2spriteInfo[spriteId];
            }
        });

        // 精灵HP变化通知事件
        emitter.on('SPRITE_HP_CHANGE', async (data) => {
            let id = data.id;
            let originHp = data.originHp;
            let hpChange = data.hpChange;
            // 在精灵上方显示伤害数字图像
            let sprite = await this.getGameObjectById(id);
            // 信息文本（originHp-hpChange）
            let text = null;
            // 文本对象
            let textObject = null;
            // 如果是减血
            if (hpChange < 0) {
                text = `${originHp}-${-hpChange}`;
                textObject = this.add.text(0, 0, text, {
                    // 粗体
                    font: "bold 26px Consolas",
                    fill: '#550000',
                });
                // 精灵也变成红色
                sprite.setTint(0xff0000);
            } else {
                text = `${originHp}+${hpChange}`;
                textObject = this.add.text(0, 0, text, {
                    font: "bold 26px Consolas",
                    // 颜色为绿色
                    fill: '#005500',
                });
                // 精灵也变成绿色
                sprite.setTint(0x00ff00);
            }
            // 设置文本的原点为中心
            textObject.setOrigin(0.5, 0);
            // 放置在map中
            this.sprite2hpMsg.set(sprite, textObject);
            // 持续时间，在指定的时间后销毁文本
            let duration = 300;
            this.time.delayedCall(duration, () => {
                this.sprite2hpMsg.delete(sprite);
                textObject.destroy();
                // 精灵恢复原来的颜色
                sprite.clearTint();
            });
        });

        // 加载完成
        this.isLoaded = true;
    }
    update() {
        // 如果还没有加载完成，则不执行更新（由于js不能阻塞，只好忙等待了）
        if (!this.isLoaded) {
            return;
        }
        // 更新层数
        for (let id in this.id2gameObject) {
            this.setDepth(this.id2gameObject[id]);
        }
        // 来自phaser.js的上游bug：
        // 一个精灵a被一个带速度的精灵b碰撞后
        // a的速度最后会一直徘徊在一个极小的值，永远不会变成0（原因未知）
        // 如果此时a尝试进行补间动画，补间动画将无法进行（原因未知）
        // 因此，这里要手动将速度过小的精灵的速度设置为0
        for (let id in this.id2gameObject) {
            let gameObject = this.id2gameObject[id];
            // 如果速度过小，就设置为0
            if (Math.abs(gameObject.body.velocity.x) < 0.1
                && Math.abs(gameObject.body.velocity.y) < 0.1) {
                gameObject.setVelocityX(0);
                gameObject.setVelocityY(0);
            }
        }

        // 更新文本的位置
        this.updateTextMsgPosition();
    }
}



export default MainScene;