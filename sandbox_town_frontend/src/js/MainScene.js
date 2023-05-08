const mainScene = {
    key: 'main',
    preload: function () {
        console.log(this);
        // this.game.add.text(0,0,"hello,123");
        // this.game.load.setBaseURL('http://localhost:8080')
        // 在这里预加载游戏资源，例如地图、角色、建筑等
        // this.load.image("player", require("@/assets/player.png"));
        // this.load.image("tree", "./assets/tree.png");
        // this.load.image("land", "./assets/land.png");
        // this.load.image("stone", "./assets/stone.png");

        // this.game.scene.scenes[0].load.image("river", "/assets/river.jpg");
        // let river = require("@/assets/river.jpg");

        this.load.image("river", require("@/assets/river.jpg"));
        // this.game.scene.scenes[0].load.image("river", require("@/assets/river.jpg"));



        // this.load.image("building", "./assets/building.png");
    },
    create: function () {
        this.add.text(0, 0, "hello,main");
        const item = this.add.image(400, 300, 'river');
        item.setInteractive();

        item.on('pointerdown', () => {
            console.log(this);
            clearScene(this);
            this.game.scene.start('store');
            this.game.events.emit('itemClicked');
        });

        // this.game.scene.scenes[0].add.text(0, 0, "hello,123");
        // this.game.scene.scenes[0].add.image(400, 300, 'river');
        // 创建地图
        createMap();

        // // 创建角色
        // this.player = this.physics.add.sprite(100, 100, "player");

        // this.camera.follow(player);

        // // 创建建筑
        // this.buildings = this.physics.add.staticGroup();
        // this.buildings.create(200, 200, "building");

        // // 设置角色与建筑下半部分的碰撞检测
        // this.physics.add.collider(this.player, this.buildings, (player, building) => {
        //     if (player.y > building.y) {
        //         player.y = building.y + building.height / 2;
        //     }
        // });

        // // 设置键盘输入监听
        // this.cursors = this.input.keyboard.createCursorKeys();
    },
    update: function () {
        // // 在这里编写游戏逻辑，例如角色移动、碰撞检测等
        // // 角色移动速度
        // const speed = 160;

        // // 根据方向键输入更新角色速度
        // if (this.cursors.left.isDown) {
        //     this.player.setVelocityX(-speed);
        // } else if (this.cursors.right.isDown) {
        //     this.player.setVelocityX(speed);
        // } else {
        //     this.player.setVelocityX(0);
        // }

        // if (this.cursors.up.isDown) {
        //     this.player.setVelocityY(-speed);
        // } else if (this.cursors.down.isDown) {
        //     this.player.setVelocityY(speed);
        // } else {
        //     this.player.setVelocityY(0);
        // }
    },
}



function createMap() {
    alert("create map");
    // this.world.setBounds(0, 0, this.worldWidth, this.worldHeight);

    // // 创建 Tilemap
    // const map = this.make.tilemap({ tileWidth: 32, tileHeight: 32, width: this.worldWidth, height: this.worldHeight });

    // // 添加地形图块集合
    // const tileset = map.addTilesetImage("land", "land");

    // // 创建地形图层
    // const terrainLayer = map.createBlankDynamicLayer("terrain", tileset);

    // // 随机生成地形
    // for (let x = 0; x < this.worldWidth; x++) {
    //     for (let y = 0; y < this.worldHeight; y++) {
    //         // 随机选择一种地形
    //         let terrain = "river";
    //         // if (Math.random() < 0.2) {
    //         //     terrain = "stone";
    //         // } else if (Math.random() < 0.1) {
    //         //     terrain = "land";
    //         // }

    //         // 在当前位置添加地形
    //         map.putTileByName(terrain, x, y, terrainLayer);
    //         // const tile = map.putTileByName(terrain, x, y, terrainLayer);
    //     }
    // }
}

function clearScene(scene) {
    // 遍历场景中的所有游戏对象
    scene.children.each(function (gameObject) {
        // 移除游戏对象上的所有事件监听器
        gameObject.removeAllListeners();
    });

    // 清空场景的游戏对象和事件
    scene.children.removeAll();
    scene.events.off();
}



export default mainScene;