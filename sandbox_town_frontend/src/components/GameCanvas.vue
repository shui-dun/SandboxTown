<template>
    <div id="game-canvas"></div>
</template>
  
<script>
import Phaser from "phaser";
import MainScene from "../js/MainScene.js";
import StoreScene from "../js/StoreScene.js";
import mixin from "@/js/mixin.js";

export default {
    mounted() {
        const config = {
            type: Phaser.AUTO,
            width: window.innerWidth * devicePixelRatio,
            height: window.innerHeight * devicePixelRatio,
            parent: "game-canvas",
            scale: {
                mode: Phaser.Scale.AUTO,
            },
            physics: {
                default: 'matter',
                matter: {
                    debug: false,
                    gravity: { y: 0 },
                }
            },
            pixelArt: false,
            scene: [MainScene, StoreScene],
        };

        this.game = new Phaser.Game(config);

        this.game.events.on('forward', (event) => {
            this.$emit(event.name, event.data);
        });

    },
    methods: {
    },
};


</script>
  
<style scoped>
#game-canvas {
    width: 100%;
    height: 99%;
    position: absolute;
    top: 0;
    left: 0;
}
</style>