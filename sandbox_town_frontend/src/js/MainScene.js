// import SimplexNoise from "perlin-simplex";
import Phaser from "phaser";

// 设置id->item的映射
var id2item = {};

var ws = null;

var player = null;

var player2 = null;

var dog = null;

var mapInfo = null;

const mainScene = {
    key: 'main',
    preload: function () {
        this.load.image("player", require("@/assets/img/player.png"));
        this.load.image("dog", require("@/assets/img/dog.png"));
        this.load.image("store", require("@/assets/img/store.png"));
        this.load.image("tree", require("@/assets/img/tree.png"));

        this.load.json('collapseShapes', require("@/assets/json/collapseShapes.json"));
        this.load.json('clickShapes', require("@/assets/json/clickShapes.json"));

    },
    create: async function () {
        let self = this;

        // 建立websocket连接
        ws = new WebSocket("ws://localhost:9090/event");

        ws.onopen = function () {
            console.log("Connection open ...");
            ws.send(JSON.stringify({
                "type": "online",
            }));
        };

        ws.onmessage = function (event) {
            console.log("Received data", JSON.parse(event.data));
            let response = JSON.parse(event.data);
            // 如果是移动
            if (response.type === 'MOVE') {
                // 物品
                let item = id2item[response.data.id];
                // 速度
                let speed = response.data.speed;
                // 路径
                let originPath = response.data.path;
                // 创建补间动画
                const path = new Phaser.Curves.Path(originPath[0], originPath[1]);
                for (let i = 2; i < originPath.length; i += 2) {
                    path.lineTo(originPath[i], originPath[i + 1]);
                }

                console.log(path, path.getLength());

                let tweenProgress = { value: 0 };

                self.tweens.add({
                    targets: tweenProgress,
                    value: 1,
                    duration: speed * path.getLength() / 4,
                    ease: 'Linear',
                    repeat: 0,
                    onRepeat: () => {
                        item.angle += 90;
                    },
                    onUpdate: () => {
                        const point = path.getPoint(tweenProgress.value);
                        self.matter.body.setPosition(item.body, { x: point.x, y: point.y });
                    },
                });
            }
        }

        ws.onerror = function (event) {
            console.log(`Connection error:`, event);
        };

        ws.onclose = function () {
            console.log("Connection closed.");
        };

        // 得到地图信息
        let mapInfo = await getMapInfo();

        // 设置地图大小
        this.matter.world.setBounds(0, 0, mapInfo.mapWidth, mapInfo.mapHeight);

        // 相机设置
        let collapseShapes = this.cache.json.get('collapseShapes');
        let clickShapes = this.cache.json.get('clickShapes');
        this.cameras.main.setBackgroundColor('#d3c6a6');
        this.cameras.main.setBounds(0, 0, mapInfo.mapWidth, mapInfo.mapHeight);

        // 创建建筑
        for (let i = 0; i < mapInfo.buildings.length; i++) {
            let building = mapInfo.buildings[i];
            // 创建建筑物
            let buildingSprite = this.matter.add.sprite(0, 0, building.type, null, { isStatic: true, shape: collapseShapes[building.type] });
            // 设置建筑物大小和位置
            buildingSprite.setDisplaySize(building.displayWidth, building.displayHeight);
            let axis = convertToCenter(buildingSprite, building.originX, building.originY);
            buildingSprite.setPosition(axis.x, axis.y);
            // 设置建筑物层级
            setDepth(buildingSprite);
            // 设置点击建筑物的事件
            buildingSprite.setInteractive({ hitArea: new Phaser.Geom.Polygon(clickShapes[building.type]), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
            buildingSprite.on('pointerdown', () => {
                this.game.events.emit('clickTarget', { "type": building.type, "targetID": building.id });
            });
        }

        // 创建角色 (user_xixi)
        player = this.matter.add.sprite(100, 100, "player", null, { shape: collapseShapes.player });
        player.setDisplaySize(120, 120);
        player.setFixedRotation();
        setDepth(player);
        id2item['user_xixi'] = player;
        this.cameras.main.startFollow(player);

        // 每一段时间向服务器发送一次位置信息
        // 只有位置变化时才发送
        // 记录上一次发送的位置
        let lastX = player.x;
        let lastY = player.y;
        setInterval(() => {
            if (lastX === player.x && lastY === player.y) {
                return;
            }
            ws.send(JSON.stringify({
                "type": "COORDINATE",
                "data": {
                    "id": "user_xixi",
                    "x": player.x,
                    "y": player.y,
                }
            }));
            lastX = player.x;
            lastY = player.y;
        }, 100);

        // 创建角色2 (user_haha)
        player2 = this.matter.add.sprite(400, 100, "player", null, { shape: collapseShapes.player });
        player2.setDisplaySize(120, 120);
        player2.setFixedRotation();
        setDepth(player2);
        player2.setInteractive({ hitArea: new Phaser.Geom.Polygon(clickShapes.player), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
        player2.on('pointerdown', () => {
            this.game.events.emit('showAttributeList', { "itemID": 'user_haha' });
        });
        id2item['user_haha'] = player2;

        // 创建狗
        dog = this.matter.add.sprite(100, 400, "dog", null, { shape: collapseShapes.dog });
        dog.setDisplaySize(120, 120);
        dog.setFixedRotation();
        setDepth(dog);
        dog.setInteractive({ hitArea: new Phaser.Geom.Polygon(clickShapes.dog), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
        dog.on('pointerdown', () => {
            // 
        });


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
            if (item1 === player && item2.body.label === 'store'
                || item1.body.label === 'store' && item2 === player) {
                if (now - lastCollisionTime < 1000) {
                    return;
                }
                this.game.events.emit('showFadeInfo', { "msg": '按空格键进入商店' });
            }
            // 如果是玩家与树木碰撞
            if (item1 === player && item2.body.label === 'tree'
                || item1.body.label === 'tree' && item2 === player) {
                if (now - lastCollisionTime < 1000) {
                    return;
                }
                this.game.events.emit('showFadeInfo', { "msg": '恭喜获得1个苹果🍎' });
            }
            lastCollisionTime = now;
            // 如果是玩家之间的碰撞
            if (item1 === player && item2 === player2) {
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
                    "x0": player.x,
                    "y0": player.y,
                    "x1": x,
                    "y1": y,
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
        setDepth(player);
        setDepth(player2);
        setDepth(dog);
        // 角色移动速度
        const speed = 8;

        // 根据方向键输入更新角色速度
        if (this.cursors.left.isDown) {
            player2.setVelocityX(-speed);
        } else if (this.cursors.right.isDown) {
            player2.setVelocityX(speed);
        } else {
            player2.setVelocityX(0);
        }

        if (this.cursors.up.isDown) {
            player2.setVelocityY(-speed);
        } else if (this.cursors.down.isDown) {
            player2.setVelocityY(speed);
        } else {
            player2.setVelocityY(0);
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

async function getMapInfo() {
    // 从后端获得建筑列表
    await fetch('/rest/map/getMapInfo', {
        method: 'GET',
    }).then(response => response.json())
        .then(data => {
            if (data.code === 0) {
                mapInfo = data.data;
            } else {
                this.fadeInfoShow(data.msg);
            }
        }).catch(error => {
            this.fadeInfoShow(`请求出错: ${error}`);
        });
    return mapInfo;
}

export default mainScene;