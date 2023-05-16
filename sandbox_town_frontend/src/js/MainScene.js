// import SimplexNoise from "perlin-simplex";
import Phaser from "phaser";

const mainScene = {
    key: 'main',
    preload: function () {
        this.load.image("player", require("@/assets/img/player.png"));
        this.load.image("dog", require("@/assets/img/dog.png"));
        this.load.image("store", require("@/assets/img/store.png"));
        this.load.image("tree", require("@/assets/img/tree.jpg"));

        this.load.json('shapes', require("@/assets/json/shape.json"));
        this.load.json('clickShapes', require("@/assets/json/clickShapes.json"));
    },
    create: function () {
        // 地图大小
        this.mapWidth = 1900;
        this.mapHeight = 1000;
        this.matter.world.setBounds(0, 0, this.mapWidth, this.mapHeight);

        // 相机设置
        let shapes = this.cache.json.get('shapes');
        let clickShapes = this.cache.json.get('clickShapes');
        this.cameras.main.setBackgroundColor('#d3c6a6');
        this.cameras.main.setBounds(0, 0, this.mapWidth, this.mapHeight);

        // 创建角色
        this.player = this.matter.add.sprite(100, 100, "player", null, { shape: shapes.player });
        this.player.setDisplaySize(120, 120);
        this.player.setFixedRotation();
        this.cameras.main.startFollow(this.player);

        this.player2 = this.matter.add.sprite(400, 100, "player", null, { shape: shapes.player });
        this.player2.setDisplaySize(120, 120);
        this.player2.setFixedRotation();
        this.player2.setInteractive({ hitArea: new Phaser.Geom.Polygon(clickShapes.player), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
        this.player2.on('pointerdown', () => {
            this.game.events.emit('showAttributeList', { "itemID": 'player2' });
        });

        // 创建狗
        this.dog = this.matter.add.sprite(100, 400, "dog", null, { shape: shapes.dog });
        this.dog.setDisplaySize(120, 120);
        this.dog.setFixedRotation();
        this.dog.setInteractive({ hitArea: new Phaser.Geom.Polygon(clickShapes.dog), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
        this.dog.on('pointerdown', () => {
            // 
        });

        // 创建树木
        this.tree = this.matter.add.sprite(300, 500, "tree", null, { isStatic: true, shape: shapes.tree });
        this.tree.setDisplaySize(400, 400);

        this.tree.setInteractive({ hitArea: new Phaser.Geom.Polygon(clickShapes.tree), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
        this.tree.on('pointerdown', () => {
            // 
        });

        // 创建商店
        this.store = this.matter.add.sprite(700, 400, "store", null, { isStatic: true, shape: shapes.store });
        this.store.setDisplaySize(250, 250);

        this.store.setInteractive({ hitArea: new Phaser.Geom.Polygon(clickShapes.store), hitAreaCallback: Phaser.Geom.Polygon.Contains, useHandCursor: true });
        this.store.on('pointerdown', () => {
            this.game.events.emit('clickStore', { "storeID": 'store' });
        });

        // 碰撞检测
        this.matter.world.on('collisionstart', (event) => {
            var pairs = event.pairs;
            for (var i = 0; i < pairs.length; i++) {
                var pair = pairs[i];
                var item1 = pair.bodyA.gameObject;
                var item2 = pair.bodyB.gameObject;
                if (item1 === null || item2 === null) {
                    continue;
                }
                // 如果是玩家与商店碰撞
                if (item1 === this.player && item2.body.label === 'store'
                    || item1.body.label === 'store' && item2 === this.player) {
                    this.game.events.emit('showFadeInfo', { "msg": '按空格键进入商店' });
                }
                // 如果是玩家与树木碰撞
                if (item1 === this.player && item2.body.label === 'tree'
                    || item1.body.label === 'tree' && item2 === this.player) {
                        this.game.events.emit('showFadeInfo', { "msg": '恭喜获得1个苹果🍎' });
                    // this.game.events.emit('showInfoModal', { "msg": "恭喜获得1个苹果🍎" });
                }
            }
        });

        // 设置键盘输入监听
        this.cursors = this.input.keyboard.createCursorKeys();

        // 层级设置
        const layer1 = this.add.layer();
        const layer2 = this.add.layer();
        layer1.add([this.player, this.player2]);
        layer2.add([this.store, this.tree])

    },
    update: function () {
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

export default mainScene;