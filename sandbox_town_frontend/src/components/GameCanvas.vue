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
            width: window.innerWidth - 7,
            height: window.innerHeight - 7,
            parent: "game-canvas",
            physics: {
                default: 'arcade',
                arcade: {
                    debug: false
                }
            },
            scene: [mainScene, storeScene],
        };

        this.game = new Phaser.Game(config);



    },
    methods: {
    },
};


</script>
  
<style scoped></style>