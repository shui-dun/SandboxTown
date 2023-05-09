<template>
    <div id="game-canvas"></div>
</template>
  
<script>
import Phaser from "phaser";
import mainScene from "../js/MainScene.js";
import storeScene from "../js/StoreScene.js";

export default {
    mounted() {
        const config = {
            type: Phaser.AUTO,
            width: window.innerWidth - 6,
            height: window.innerHeight - 6,
            parent: "game-canvas",
            scene: [mainScene, storeScene],
        };

        this.game = new Phaser.Game(config);

        
        
    },
    methods: {
    },
};


</script>
  
<style scoped></style>