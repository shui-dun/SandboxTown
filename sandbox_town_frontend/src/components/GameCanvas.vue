<template>
    <div id="game-canvas"></div>
</template>
  
<script>
import Phaser from "phaser";
import mainScene from "../js/MainScene.js";
import storeScene from "../js/StoreScene.js";

export default {
    inject: ['fadeInfoShow'],
    mounted() {
        const config = {
            type: Phaser.AUTO,
            width: window.innerWidth,
            height: window.innerHeight - 7,
            parent: "game-canvas",
            physics: {
                default: 'matter',
                matter: {
                    debug: false,
                    gravity: { y: 0 },
                }
            },
            pixelArt: false,
            scene: [mainScene, storeScene],
        };

        this.game = new Phaser.Game(config);

        this.game.events.on('showFadeInfo', (event) => {
            this.fadeInfoShow(event.msg);
        });

        this.game.events.on('showAttributeList', (event) => {
            this.$emit('showAttributeList', event.itemID);
        });

        this.game.events.on('ArriveAtTarget', (event) => {
            if (event.type === 'tree') {
                let msg = {
                    duration: 5,
                    text: '正在摘苹果...',
                    progressCompleteEvent: () => {
                        this.fadeInfoShow('恭喜获得一个苹果！');
                    },
                }
                this.$emit('processBarShow', msg);
            } else if (event.type == 'store') {
                this.$emit('showStore', event.targetID);
            }
        });

    },
    methods: {
    },
};


</script>
  
<style scoped></style>