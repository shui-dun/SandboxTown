<template>
    <div id="game-canvas"></div>
</template>
  
<script>
import Phaser from "phaser";
import mainScene from "../js/MainScene.js";
import storeScene from "../js/StoreScene.js";

export default {
    mounted() {
        this.sceneWidth = window.innerWidth - 8;
        this.sceneHeight = window.innerHeight - 8;
        this.worldWidth = 2000;
        this.worldHeight = 2000;
        console.log(this);
        const config = {
            type: Phaser.AUTO,
            width: this.sceneWidth,
            height: this.sceneHeight,
            parent: "game-canvas",
            scene: [mainScene, storeScene],
        };

        this.game = new Phaser.Game(config);

        // 事件处理
        this.game.events.on('itemClicked', () => {
            this.$emit('itemClicked');
        });
    },
    methods: {
    },
};


</script>
  
<style scoped></style>