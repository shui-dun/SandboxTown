<template>
    <div id="game-canvas"></div>
</template>
  
<script>
import Phaser from "phaser";
import {mainScene, closeGame} from "../js/MainScene.js";
import storeScene from "../js/StoreScene.js";
import myUtils from "@/js/myUtils.js";

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
            scene: [mainScene, storeScene],
        };

        this.game = new Phaser.Game(config);

        this.game.events.on('showFadeInfo', (event) => {
            myUtils.fadeInfoShow(event.msg);
        });

        this.game.events.on('showAttributePannel', (event) => {
            this.$emit('showAttributePannel', event.itemID);
        });

        this.game.events.on('ArriveAtTarget', (event) => {
            if (event.type === 'tree') {
                let msg = {
                    duration: 5,
                    text: '正在摘苹果...',
                    progressCompleteEvent: () => {
                        // 向后端发送摘苹果请求
                        myUtils.myPOST('/rest/tree/pickApple',
                            new URLSearchParams({
                                treeId: event.targetID,
                            }),
                            () => {
                                myUtils.fadeInfoShow('摘苹果成功');
                            },
                        );
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
    unmounted() {
        this.game.destroy(true);
        closeGame();
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