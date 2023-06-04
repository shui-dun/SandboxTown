import Phaser from "phaser";

// 设置id->gameObject的映射
var id2gameObject = {};

// websocket连接
var ws = null;

// 地图信息
var mapInfo = null;

var myUsername = null;

// 角色列表
var characterList = [];

const mainScene = {
    key: 'main',
    preload: function () {
        this.load.image("user", require("@/assets/img/user.png"));
        this.load.image("dog", require("@/assets/img/dog.png"));
        this.load.image("store", require("@/assets/img/store.png"));
        this.load.image("tree", require("@/assets/img/tree.png"));

        // 加载纹理图片
        this.load.spritesheet("tiles", require("@/assets/img/tiles.png"), { frameWidth: 128, frameHeight: 128, endFrame: 11 });

        this.load.json('collapseShapes', require("@/assets/json/collapseShapes.json"));
        this.load.json('clickShapes', require("@/assets/json/clickShapes.json"));

    },
    create: async function () {
        let self = this;

        // 得到地图信息
        mapInfo = await getMapInfo();

        // 得到当前用户的用户名
        myUsername = await getMyUsername();

        // 得到角色列表
        characterList = await getCharacterList();

        // 建立websocket连接
        ws = new WebSocket("ws://localhost:9090/event");

        ws.onopen = function () {
            console.log("Connection open ...");
            ws.send(JSON.stringify({
                "type": "online",
            }));
        };

        let lastTween = null;
        ws.onmessage = function (event) {
            console.log("Received data", JSON.parse(event.data));
            let response = JSON.parse(event.data);
            // 如果是移动
            if (response.type === 'MOVE') {
                // 物品
                let item = id2gameObject[response.data.id];
                // 速度
                let speed = response.data.speed;
                // 路径
                let originPath = response.data.path;
                // 终点id
                let dest_id = response.data.dest_id;
                // 目的地的到达事件
                let arriveEvent = () => {
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
                if (dest_id != null && mapInfo.buildingTypes.map(item => item.id).indexOf(dest_id.split("_", 2)[0]) != -1) {
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
                        self.matter.body.setPosition(item.body, { x: point.x, y: point.y });
                    },
                    onComplete: () => {
                        if (this.isStopped) {
                            return;
                        }
                        arriveEvent();
                    }
                });
                lastTween = tween;
            }
        }

        ws.onerror = function (event) {
            console.log(`Connection error:`, event);
        };

        ws.onclose = function () {
            console.log("Connection closed.");
        };

        // 设置地图大小
        this.matter.world.setBounds(0, 0, mapInfo.mapWidth, mapInfo.mapHeight);

        // 相机设置
        let collapseShapes = this.cache.json.get('collapseShapes');
        let clickShapes = this.cache.json.get('clickShapes');
        this.cameras.main.setBackgroundColor('#c1d275');
        this.cameras.main.setBounds(0, 0, mapInfo.mapWidth, mapInfo.mapHeight);

        // 遍历每个区域，创建背景纹理
        let textureLen = 75;
        for (let i = 0; i < mapInfo.mapWidth / textureLen; i++) {
            for (let j = 0; j < mapInfo.mapHeight / textureLen; j++) {
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

        // 创建建筑
        for (let i = 0; i < mapInfo.buildings.length; i++) {
            let building = mapInfo.buildings[i];
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
        for (let i = 0; i < characterList.length; i++) {
            let character = characterList[i];
            // 创建角色
            let characterSprite = this.matter.add.sprite(0, 0, character.type, null, { shape: collapseShapes[character.type] });
            // 设置角色大小和位置
            characterSprite.setDisplaySize(character.width, character.height);
            characterSprite.setPosition(character.x, character.y);
            // 设置角色层级
            setDepth(characterSprite);
            // 禁止旋转
            characterSprite.setFixedRotation();
            // 设置点击角色的事件
            characterSprite.setInteractive({ hitArea: new Phaser.Geom.Polygon(clickShapes[character.type]), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
            characterSprite.on('pointerdown', (pointer, _localX, _localY, event) => {
                this.game.events.emit('showAttributeList', { "itemID": character.id });
                // 阻止事件冒泡
                event.stopPropagation();
            });
            // 放置到字典中
            id2gameObject[character.id] = characterSprite;
        }

        // 相机跟随自己
        this.cameras.main.startFollow(id2gameObject[myUsername]);

        // 每一段时间向服务器发送一次角色位置信息
        // 只发送所有人为自己或者空的角色的坐标信息
        // 记录上一次发送的位置
        // let lastAxisMap = {}
        // setInterval(() => {
        //     // 遍历所有角色
        //     for (let id in id2gameObject) {
        //         // 如果角色的所有者是自己或者空
        //         if (id2gameObject[id].owner === myUsername || id2gameObject[id].owner === '') {
        //     // 只有位置变化时才发送

        //     ws.send(JSON.stringify({
        //         "type": "COORDINATE",
        //         "data": {
        //             "id": myUsername,
        //             "x": player.x,
        //             "y": player.y,
        //         }
        //     }));
        //     lastX = player.x;
        //     lastY = player.y;
        // }, 100);


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
            // 如果是玩家与树木碰撞
            if (item1 === id2gameObject[myUsername] && item2.body.label === 'tree'
                || item1.body.label === 'tree' && item2 === id2gameObject[myUsername]) {
                if (now - lastCollisionTime < 1000) {
                    return;
                }
                this.game.events.emit('showFadeInfo', { "msg": '恭喜获得1个苹果🍎' });
            }
            lastCollisionTime = now;
            // 如果是玩家之间的碰撞
            if (item1 === id2gameObject[myUsername] && item1.body.label === 'user') {
                this.game.events.emit('showFadeInfo', { "msg": '你好，我是user_haha' });
            }
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
            console.log('click at: ' + x + ', ' + y);
        });
    },
    update: function () {
        // 如果地图信息还没有加载完成，则不执行更新（由于js不能阻塞，只好忙等待了）
        if (mapInfo === null) {
            return;
        }
        // 更新层数
        for (let id in id2gameObject) {
            setDepth(id2gameObject[id]);
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

async function getMapInfo() {
    let mapInfo = null;
    // 从后端获得建筑列表
    await fetch('/rest/map/getMapInfo', {
        method: 'GET',
    }).then(response => response.json())
        .then(data => {
            if (data.code === 0) {
                // 得到地图信息
                mapInfo = data.data;
            } else {
                this.fadeInfoShow(data.msg);
            }
        }).catch(error => {
            this.fadeInfoShow(`请求出错: ${error}`);
        });
    return mapInfo;
}

// 从后端获得角色列表
async function getCharacterList() {
    let characterList = null;
    // 从后端获得角色列表
    await fetch('/rest/character/listAll', {
        method: 'GET',
    }).then(response => response.json())
        .then(data => {
            if (data.code === 0) {
                // 得到角色列表
                characterList = data.data;
            } else {
                this.fadeInfoShow(data.msg);
            }
        }).catch(error => {
            this.fadeInfoShow(`请求出错: ${error}`);
        });
    return characterList;
}

export default mainScene;