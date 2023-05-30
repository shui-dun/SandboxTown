// import SimplexNoise from "perlin-simplex";
import Phaser from "phaser";

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
    create: function () {
        // 得到地图信息
        getMapInfo().then((mapInfo) => {
            this.mapInfo = mapInfo;

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

            // 创建角色
            this.player = this.matter.add.sprite(100, 100, "player", null, { shape: collapseShapes.player });
            this.player.setDisplaySize(120, 120);
            this.player.setFixedRotation();
            setDepth(this.player);
            this.cameras.main.startFollow(this.player);

            this.player2 = this.matter.add.sprite(400, 100, "player", null, { shape: collapseShapes.player });
            this.player2.setDisplaySize(120, 120);
            this.player2.setFixedRotation();
            setDepth(this.player2);
            this.player2.setInteractive({ hitArea: new Phaser.Geom.Polygon(clickShapes.player), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
            this.player2.on('pointerdown', () => {
                this.game.events.emit('showAttributeList', { "itemID": 'user_haha' });
            });

            // 创建狗
            this.dog = this.matter.add.sprite(100, 400, "dog", null, { shape: collapseShapes.dog });
            this.dog.setDisplaySize(120, 120);
            this.dog.setFixedRotation();
            setDepth(this.dog);
            this.dog.setInteractive({ hitArea: new Phaser.Geom.Polygon(clickShapes.dog), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
            this.dog.on('pointerdown', () => {
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
                if (item1 === this.player && item2.body.label === 'store'
                    || item1.body.label === 'store' && item2 === this.player) {
                    if (now - lastCollisionTime < 1000) {
                        return;
                    }
                    this.game.events.emit('showFadeInfo', { "msg": '按空格键进入商店' });
                }
                // 如果是玩家与树木碰撞
                if (item1 === this.player && item2.body.label === 'tree'
                    || item1.body.label === 'tree' && item2 === this.player) {
                    if (now - lastCollisionTime < 1000) {
                        return;
                    }
                    this.game.events.emit('showFadeInfo', { "msg": '恭喜获得1个苹果🍎' });
                }
                lastCollisionTime = now;
            });

            // 设置键盘输入监听
            this.cursors = this.input.keyboard.createCursorKeys();
        });
    },
    update: function () {
        // 如果地图信息还没有加载完成，则不执行更新（由于js不能阻塞，只好忙等待了）
        if (this.mapInfo === undefined) {
            return;
        }
        // 更新层数
        setDepth(this.player);
        setDepth(this.player2);
        setDepth(this.dog);
        // 在这里编写游戏逻辑，例如角色移动、碰撞检测等
        // 角色移动速度
        const speed = 8;

        // 根据方向键输入更新角色速度
        if (this.cursors.left.isDown) {
            this.player.setVelocityX(-speed);
        } else if (this.cursors.right.isDown) {
            this.player.setVelocityX(speed);
        } else {
            this.player.setVelocityX(0);
        }

        if (this.cursors.up.isDown) {
            this.player.setVelocityY(-speed);
        } else if (this.cursors.down.isDown) {
            this.player.setVelocityY(speed);
        } else {
            this.player.setVelocityY(0);
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
    let mapInfo = {};
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