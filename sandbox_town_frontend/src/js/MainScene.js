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

        this.load.image('bg', require('@/assets/img/bg.jpg'));
    },
    create: function () {
        // 地图大小
        this.mapWidth = 1900;
        this.mapHeight = 1000;
        this.matter.world.setBounds(0, 0, this.mapWidth, this.mapHeight);

        // 相机设置
        let collapseShapes = this.cache.json.get('collapseShapes');
        let clickShapes = this.cache.json.get('clickShapes');
        this.cameras.main.setBackgroundColor('#d3c6a6');
        this.cameras.main.setBounds(0, 0, this.mapWidth, this.mapHeight);

        // 获取游戏的宽度和高度
        const gameWidth = this.scale.width;
        const gameHeight = this.scale.height;

        // 计算需要铺满背景的贴图数量
        const tilesX = Math.ceil(gameWidth / 500);
        const tilesY = Math.ceil(gameHeight / 500);

        // 使用循环创建 TileSprite 对象，将其大小设置为 500x500，并铺满整个游戏背景
        for (let i = 0; i <= tilesX; i++) {
            for (let j = 0; j <= tilesY; j++) {
                let tmp = this.add.image(i * 500, j * 500,'bg');
                tmp.setDisplaySize(500, 500);
            }
        }

        // 创建树木
        this.tree = this.matter.add.sprite(300, 500, "tree", null, { isStatic: true, shape: collapseShapes.tree });
        this.tree.setDisplaySize(400, 400);

        this.tree.setInteractive({ hitArea: new Phaser.Geom.Polygon(clickShapes.tree), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
        this.tree.on('pointerdown', () => {
            this.game.events.emit('clickTree', { "treeID": 'tree' });
        });
        setDepth(this.tree);

        // 创建商店
        this.store = this.matter.add.sprite(700, 400, "store", null, { isStatic: true, shape: collapseShapes.store });
        this.store.setDisplaySize(250, 250);

        this.store.setInteractive({ hitArea: new Phaser.Geom.Polygon(clickShapes.store), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
        this.store.on('pointerdown', () => {
            this.game.events.emit('clickStore', { "storeID": 'store' });
        });
        setDepth(this.store);

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
            this.game.events.emit('showAttributeList', { "itemID": 'player2' });
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
    },
    update: function () {
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

function setDepth(gameObject) {
    // shape中心的y坐标
    gameObject.setDepth(gameObject.y);
}

export default mainScene;