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
                        // 向后端发送摘苹果请求
                        fetch('/rest/tree/pickApple', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/x-www-form-urlencoded',
                            },
                            body: new URLSearchParams({
                                treeId: event.targetID,
                            }),
                        }).then((response) => response.json())
                            .then((data) => {
                                if (data.code == 0) {
                                    this.fadeInfoShow('摘苹果成功');
                                } else {
                                    this.fadeInfoShow(data.msg);
                                }
                            })
                            .catch(error => {
                                this.fadeInfoShow(`请求出错: ${error}`);
                            });
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
  
<style scoped>
#game-canvas {
    width: 100%;
    height: 99%;
    position: absolute;
    top: 0;
    left: 0;
}
</style>