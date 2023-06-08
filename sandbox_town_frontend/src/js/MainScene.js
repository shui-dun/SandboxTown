import Phaser from "phaser";

// 设置id->gameObject的映射
var id2gameObject = {};

// 设置id->sprite的映射
var id2sprite = {};

// websocket连接
var ws = null;

// 地图信息
var gameMap = null;

// 当前用户的用户名
var myUsername = null;

// 角色列表
var spriteList = [];

// 建筑类型列表
var buildingTypes = [];

// 建筑列表
var buildingList = [];

// 是否加载完成
var isLoaded = false;

const mainScene = {
    key: 'main',
    preload: function () {
        this.load.image("user", require("@/assets/img/user.png"));
        this.load.image("dog", require("@/assets/img/dog.png"));
        this.load.image("cat", require("@/assets/img/cat.png"));
        this.load.image("store", require("@/assets/img/store.png"));
        this.load.image("tree", require("@/assets/img/tree.png"));

        // 围墙
        this.load.image("wall", require("@/assets/img/wall.png"));

        // 加载纹理图片
        this.load.spritesheet("tiles", require("@/assets/img/tiles.png"), { frameWidth: 128, frameHeight: 128, endFrame: 11 });

        this.load.json('collapseShapes', require("@/assets/json/collapseShapes.json"));
        this.load.json('clickShapes', require("@/assets/json/clickShapes.json"));

    },
    create: async function () {
        // 由于js的函数内部this指向会变化，所以先将this保存到self中
        let self = this;

        // 防止右键点击时浏览器的默认行为（例如显示上下文菜单）
        self.input.mouse.disableContextMenu();

        // 得到地图信息
        gameMap = await getGameMap();

        // 得到当前用户的用户名
        myUsername = await getMyUsername();

        // 得到角色列表
        spriteList = await getSpriteList();

        // 得到建筑类型列表
        buildingTypes = await getBuildingTypeList();

        // 得到建筑列表
        buildingList = await getBuildingList();

        // 建立websocket连接
        ws = new WebSocket("ws://localhost:9090/event");

        // 加载完成
        isLoaded = true;

        ws.onopen = function () {
            console.log("Connection open ...");
            ws.send(JSON.stringify({
                "type": "online",
            }));
        };

        let lastTween = null;
        ws.onmessage = function (event) {
            let response = JSON.parse(event.data);
            // 如果是移动
            if (response.type === 'MOVE') {
                // 移动事件的发起者
                let initatorSprite = id2sprite[response.data.id];
                // 物品
                let initatorGameObject = id2gameObject[response.data.id];
                // 速度
                let speed = response.data.speed;
                // 路径
                let originPath = response.data.path;
                // 终点id
                let dest_id = response.data.dest_id;
                // 目的地的到达事件
                let arriveEvent = () => {
                    // 如果是其他玩家或者其他玩家的宠物，就不触发到达事件
                    if ((initatorSprite.id.startsWith("user") && initatorSprite.id != myUsername) ||
                        (initatorSprite.owner != null && initatorSprite.owner != myUsername)) {
                        return;
                    }
                    if (dest_id != null) {
                        self.game.events.emit('ArriveAtTarget', { "type": dest_id.split("_", 2)[0], "targetID": dest_id });
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
                // 如果终点类型是建筑，提前几步终止，防止到达终点后因为卡进建筑而抖动
                if (dest_id != null && buildingTypes.map(item => item.id).indexOf(dest_id.split("_", 2)[0]) != -1) {
                    lastPos -= 6;
                }
                // 如果路径长度为0，就直接到达终点
                if (lastPos <= 2) {
                    arriveEvent();
                    return;
                }
                for (let i = 2; i < lastPos; i += 2) {
                    path.lineTo(originPath[i], originPath[i + 1]);
                }
                let tweenProgress = { value: 0 };
                if (lastTween != null) {
                    // 如果上一个补间动画还没结束，就停止上一个补间动画
                    lastTween.stop();
                }
                let tween = self.tweens.add({
                    targets: tweenProgress,
                    value: 1,
                    duration: speed * path.getLength() / 4,
                    ease: 'Linear',
                    repeat: 0,
                    onUpdate: () => {
                        const point = path.getPoint(tweenProgress.value);
                        self.matter.body.setPosition(initatorGameObject.body, { x: point.x, y: point.y });
                    },
                    onComplete: () => {
                        if (this.isStopped) {
                            return;
                        }
                        arriveEvent();
                    }
                });
                lastTween = tween;
            } else if (response.type === 'COORDINATE') { // 如果是坐标通知
                // 游戏对象
                let gameObject = id2gameObject[response.data.id];
                // 更新其坐标
                self.matter.body.setPosition(gameObject.body, { x: response.data.x, y: response.data.y });
            }
        }

        ws.onerror = function (event) {
            console.log(`Connection error:`, event);
        };

        ws.onclose = function () {
            console.log("Connection closed.");
        };

        // 设置地图大小
        this.matter.world.setBounds(0, 0, gameMap.width, gameMap.height);

        // 相机设置
        let collapseShapes = this.cache.json.get('collapseShapes');
        let clickShapes = this.cache.json.get('clickShapes');
        this.cameras.main.setBackgroundColor('#c1d275');
        this.cameras.main.setBounds(0, 0, gameMap.width, gameMap.height);

        // 遍历每个区域，创建背景纹理
        let textureLen = 75;
        for (let i = 0; i < gameMap.width / textureLen; i++) {
            for (let j = 0; j < gameMap.height / textureLen; j++) {
                // 一定概率创建纹理
                if (Math.random() > 0.05) {
                    continue;
                }
                let randomNum1 = Math.floor(Math.random() * 21) - 10;
                let randomNum2 = Math.floor(Math.random() * 21) - 10;
                const texture = this.add.sprite(i * textureLen + randomNum1, j * textureLen + randomNum2, 'tiles', Math.floor(Math.random() * 12));
                texture.setDisplaySize(textureLen, textureLen);
            }
        }

        // 创建围墙
        let pixelsPerGrid = 30;
        for (let x = 0; x < gameMap.data.length; ++x) {
            for (let y = 0; y < gameMap.data[0].length; ++y) {
                if (gameMap.data[x][y] == 1) {
                    const texture = this.matter.add.sprite(x * pixelsPerGrid, y * pixelsPerGrid, 'wall', null, { isStatic: true, shape: collapseShapes["wall"] })
                    texture.setDisplaySize(pixelsPerGrid, pixelsPerGrid);
                }
            }
        }


        // 创建建筑
        for (let i = 0; i < buildingList.length; i++) {
            let building = buildingList[i];
            // 创建建筑物
            let buildingSprite = this.matter.add.sprite(0, 0, building.type, null, { isStatic: true, shape: collapseShapes[building.type] });
            // 设置建筑物大小和位置
            buildingSprite.setDisplaySize(building.width, building.height);
            let axis = convertToCenter(buildingSprite, building.originX, building.originY);
            buildingSprite.setPosition(axis.x, axis.y);
            // 设置建筑物层级
            setDepth(buildingSprite);
            // 设置点击建筑物的事件
            buildingSprite.setInteractive({ hitArea: new Phaser.Geom.Polygon(clickShapes[building.type]), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
            buildingSprite.on('pointerdown', (pointer, _localX, _localY, event) => {
                const worldPoint = this.cameras.main.getWorldPoint(pointer.x, pointer.y);
                const x = worldPoint.x;
                const y = worldPoint.y;
                // 发送移动请求
                ws.send(JSON.stringify({
                    "type": "MOVE",
                    "data": {
                        "x0": id2gameObject[myUsername].x,
                        "y0": id2gameObject[myUsername].y,
                        "x1": x,
                        "y1": y,
                        "dest_id": building.id,
                    }
                }));
                // 阻止事件冒泡
                event.stopPropagation();

            });
        }

        // 创建所有角色
        for (let i = 0; i < spriteList.length; i++) {
            let sprite = spriteList[i];
            // 将其加入id2sprite
            id2sprite[sprite.id] = sprite;
            // 创建角色
            let spriteSprite = this.matter.add.sprite(0, 0, sprite.type, null, { shape: collapseShapes[sprite.type] });
            // 设置角色大小和位置
            spriteSprite.setDisplaySize(sprite.width, sprite.height);
            spriteSprite.setPosition(sprite.x, sprite.y);
            // 设置角色层级
            setDepth(spriteSprite);
            // 禁止旋转
            spriteSprite.setFixedRotation();
            // 设置点击角色的事件
            spriteSprite.setInteractive({ hitArea: new Phaser.Geom.Polygon(clickShapes[sprite.type]), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
            spriteSprite.on('pointerdown', (pointer, _localX, _localY, event) => {
                // 鼠标左键点击
                if (pointer.button === 0) {
                    this.game.events.emit('showAttributeList', { "itemID": sprite.id });
                } else if (pointer.button === 2) { // 鼠标右键点击
                    // TO-DO: 发送攻击请求
                }
                // 防止右键点击时浏览器的默认行为（例如显示上下文菜单）
                self.input.mouse.disableContextMenu();
                // 阻止事件冒泡
                event.stopPropagation();
            });
            // 放置到字典中
            id2gameObject[sprite.id] = spriteSprite;
        }

        // 相机跟随自己
        this.cameras.main.startFollow(id2gameObject[myUsername]);

        // 由于精灵被推动时，或是播放补间动画tween时，它的物理引擎不会更新其速度，速度都是0，因此在找到方法前，只同步位置，不同步速度
        // 每一段时间向服务器发送一次角色位置信息
        // 只发送自己、主人是自己、公共NPC（例如蜘蛛）的角色的坐标信息
        // 记录上一次发送的位置
        let lastAxisMap = {}
        setInterval(() => {
            // 遍历所有角色
            for (let id in id2sprite) {
                // 如果角色是自己、主人是自己、公共NPC（例如蜘蛛）
                if (id === myUsername ||
                    id2sprite[id].owner === myUsername ||
                    (id2sprite[id].owner == null && id2sprite[id].type !== "user")) {
                    // 如果上一次发送的位置和当前位置不同
                    if (lastAxisMap[id] == null ||
                        lastAxisMap[id].x !== id2gameObject[id].x ||
                        lastAxisMap[id].y !== id2gameObject[id].y) {
                        // 发送坐标信息
                        ws.send(JSON.stringify({
                            "type": "COORDINATE",
                            "data": {
                                "id": id,
                                "x": id2gameObject[id].x,
                                "y": id2gameObject[id].y,
                            }
                        }));
                        // 更新上一次发送的位置
                        lastAxisMap[id] = {
                            "x": id2gameObject[id].x,
                            "y": id2gameObject[id].y,
                        }
                    }
                }
            }
        }, 100);


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
            if (item1 === id2gameObject[myUsername] && item2.body.label === 'store'
                || item1.body.label === 'store' && item2 === id2gameObject[myUsername]) {
                if (now - lastCollisionTime < 1000) {
                    return;
                }
                this.game.events.emit('showFadeInfo', { "msg": '按空格键进入商店' });
            }
            lastCollisionTime = now;
        });

        // 设置键盘输入监听
        this.cursors = this.input.keyboard.createCursorKeys();

        // 添加点击事件
        this.input.on('pointerdown', function (pointer) {
            const worldPoint = this.cameras.main.getWorldPoint(pointer.x, pointer.y);
            const x = worldPoint.x;
            const y = worldPoint.y;
            // 发送移动请求
            ws.send(JSON.stringify({
                "type": "MOVE",
                "data": {
                    "x0": id2gameObject[myUsername].x,
                    "y0": id2gameObject[myUsername].y,
                    "x1": x,
                    "y1": y,
                    "dest_id": null,
                }
            }));
            // 防止右键点击时浏览器的默认行为（例如显示上下文菜单）
            self.input.mouse.disableContextMenu();
        });

        // 创建小地图
        // this.minimap = this.cameras.add(0, 0, 300, 150).setZoom(0.05).setName('mini');
        // this.minimap.setBackgroundColor('c1d275');
        // this.minimap.startFollow(id2gameObject[myUsername]);
        
    },
    update: function () {
        // 如果还没有加载完成，则不执行更新（由于js不能阻塞，只好忙等待了）
        if (!isLoaded) {
            return;
        }
        // 更新层数
        for (let id in id2gameObject) {
            setDepth(id2gameObject[id]);
        }
        // // 根据方向键输入更新角色速度
        let me = id2gameObject[myUsername];
        let speed = id2sprite[myUsername].speed * 0.8;
        if (this.cursors.left.isDown) {
            me.setVelocityX(-speed);
        } else if (this.cursors.right.isDown) {
            me.setVelocityX(speed);
        } else {
            me.setVelocityX(0);
        }
        if (this.cursors.up.isDown) {
            me.setVelocityY(-speed);
        } else if (this.cursors.down.isDown) {
            me.setVelocityY(speed);
        } else {
            me.setVelocityY(0);
        }
    },
}

// 设置物体的层数，层数越高，显示越靠前
function setDepth(gameObject) {
    // shape中心的y坐标
    gameObject.setDepth(gameObject.y);
}

// 将图像左上角坐标转化为物体质心坐标
function convertToCenter(gameObject, x, y) {
    let massOffsetX = gameObject.body.centerOffset.x;
    let massOffsetY = gameObject.body.centerOffset.y;
    let massX = x + massOffsetX * gameObject.body.scale.x;
    let massY = y + massOffsetY * gameObject.body.scale.y;
    return { x: massX, y: massY };
}

// 得到自己的用户名
async function getMyUsername() {
    let myUsername = null;
    // 从后端获得自己的用户名
    await fetch('/rest/user/getUsername', {
        method: 'GET',
    }).then(response => response.json())
        .then(data => {
            if (data.code === 0) {
                // 得到自己的用户名
                myUsername = data.data;
            } else {
                this.fadeInfoShow(data.msg);
            }
        }).catch(error => {
            this.fadeInfoShow(`请求出错: ${error}`);
        });
    return myUsername;
}

async function getGameMap() {
    let result = null;
    // 从后端获得建筑列表
    await fetch('/rest/gamemap/getGameMap', {
        method: 'GET',
    }).then(response => response.json())
        .then(data => {
            if (data.code === 0) {
                // 得到地图信息
                result = data.data;
            } else {
                this.fadeInfoShow(data.msg);
            }
        }).catch(error => {
            this.fadeInfoShow(`请求出错: ${error}`);
        });
    return result;
}

// 从后端获得角色列表
async function getSpriteList() {
    let spriteList = null;
    // 从后端获得角色列表
    await fetch('/rest/sprite/listAll', {
        method: 'GET',
    }).then(response => response.json())
        .then(data => {
            if (data.code === 0) {
                // 得到角色列表
                spriteList = data.data;
            } else {
                this.fadeInfoShow(data.msg);
            }
        }).catch(error => {
            this.fadeInfoShow(`请求出错: ${error}`);
        });
    return spriteList;
}

// 从后端获得建筑类型列表
async function getBuildingTypeList() {
    let buildingTypeList = null;
    // 从后端获得建筑类型列表
    await fetch('/rest/building/getAllBuildingTypes', {
        method: 'GET',
    }).then(response => response.json())
        .then(data => {
            if (data.code === 0) {
                // 得到建筑类型列表
                buildingTypeList = data.data;
            } else {
                this.fadeInfoShow(data.msg);
            }
        }).catch(error => {
            this.fadeInfoShow(`请求出错: ${error}`);
        });
    return buildingTypeList;
}

// 从后端获得建筑列表
async function getBuildingList() {
    let buildingList = null;
    // 从后端获得建筑列表
    await fetch('/rest/building/getAllBuildings', {
        method: 'GET',
    }).then(response => response.json())
        .then(data => {
            if (data.code === 0) {
                // 得到建筑列表
                buildingList = data.data;
            } else {
                this.fadeInfoShow(data.msg);
            }
        }).catch(error => {
            this.fadeInfoShow(`请求出错: ${error}`);
        });
    return buildingList;
}

export default mainScene;