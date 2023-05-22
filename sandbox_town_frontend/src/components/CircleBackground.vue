<template>
    <div v-show="ready" id="circle-bg"></div>
</template>
  
<script>
import Phaser from "phaser";

export default {
    data() {
        return {
            ready: false,
        };
    },
    mounted() {
        const config = {
            type: Phaser.AUTO,
            width: window.innerWidth - 10,
            height: window.innerHeight - 10,
            backgroundColor: "#ffffff",
            parent: "circle-bg",
            physics: {
                default: "arcade",
                arcade: {
                    gravity: { y: 0 },
                    debug: false,
                },
            },
            scene: {
                preload() {
                    // 生成圆形纹理
                    const circleCanvas = this.textures.createCanvas('circle', 128, 128);
                    const ctx = circleCanvas.context;
                    ctx.fillStyle = '#ffffff';
                    ctx.arc(64, 64, 64, 0, Math.PI * 2, false);
                    ctx.fill();
                    circleCanvas.refresh();
                },
                create() {
                    // 添加图形组
                    this.circles = this.physics.add.group({
                        key: "circle",
                        frameQuantity: 5,
                    });

                    // 放置在随机位置
                    Phaser.Actions.RandomRectangle(
                        this.circles.getChildren(),
                        new Phaser.Geom.Rectangle(0, 0, window.innerWidth, window.innerHeight)
                    );

                    // 随机设置圆形的大小、弹跳力、速度、颜色，并设置与边界的碰撞
                    this.circles.children.iterate((circle) => {
                        const radius = Phaser.Math.Between(10, 64);
                        circle.setDisplaySize(radius * 2, radius * 2);
                        circle.setCircle(circle.width / 2);
                        circle.setBounce(1, 1);
                        circle.setCollideWorldBounds(true);
                        circle.setVelocity(Phaser.Math.Between(-200, 200), Phaser.Math.Between(-200, 200));
                        circle.setTint(Phaser.Math.Between(0x00000, 0xffffff));
                    });

                    // 设置圆形之间的碰撞
                    this.physics.add.collider(this.circles, this.circles);
                },
                update: () => { },
            },
        };

        this.game = new Phaser.Game(config);

        this.game.events.on('ready', () => {
            this.ready = true;
        });
    },
    methods: {
    },
};
</script>