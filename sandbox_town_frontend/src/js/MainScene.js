import SimplexNoise from "perlin-simplex";

const mainScene = {
    key: 'main',
    preload: function () {
        this.load.image("player", require("@/assets/player.png"));
        this.load.image("river", require("@/assets/river.jpg"));
        this.load.image("grass", require("@/assets/grass.jpg"));
        this.load.image("land", require("@/assets/land.jpg"));

    },
    create: function () {
        // 创建地图
        createMap(this);

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



function createMap(scene) {
    // 地图大小
    scene.mapWidth = 2000;
    scene.mapHeight = 2000;
    // scene.world.setBounds(0, 0, scene.mapWidth, scene.mapHeight);

    // 地形纹理
    // const textures = ["land", "stone", "river"];

    // 创建地图
    let simplex = new SimplexNoise();
    for (let x = 0; x < scene.mapWidth; x += 32) {
        for (let y = 0; y < scene.mapHeight; y += 32) {
            // 使用 Perlin 噪声生成随机值
            const noiseValue = simplex.noise(x * 0.1, y * 0.1);

            // 根据噪声值选择地形纹理
            let texture;
            if (noiseValue < 0.3) {
                texture = "land";
            } else if (noiseValue < 0.6) {
                texture = "river";
            } else {
                texture = "grass";
            }

            // 在当前位置创建地形
            let item = scene.add.image(x, y, texture,);
            item.displayWidth = 32;
            item.displayHeight = 32;
        }
    }
}



export default mainScene;