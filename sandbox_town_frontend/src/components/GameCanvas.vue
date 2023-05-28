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

        this.game.events.on('clickStore', (event) => {
            this.$emit('showStore', event.storeID);
        });

        this.game.events.on('clickTree', (event) => {
            let msg = {
                duration: 5,
                text: '正在摘苹果...',
                progressCompleteEvent: () => {
                    this.$emit('showFadeInfo', '恭喜获得一个苹果！');
                },
            }
            this.$emit('processBarShow', msg);
        });


        // 测试websocket
        var ws = new WebSocket("ws://localhost:9090/event");

        ws.onopen = function () {
            console.log("Connection open ...");
            ws.send(JSON.stringify({
                "type": "foo",
                "data": {
                    "xixi": "haha",
                    "hehe": "nani"
                }
            }));
        };

        ws.onmessage = function (event) {
            console.log("Received data");
            console.log(JSON.parse(event.data));
        }

        ws.onerror = function (event) {
            console.log(`Connection error:`, event);
        };

        ws.onclose = function () {
            console.log("Connection closed.");
        };



    },
    methods: {
        // 得到指定用户属性信息
        getAttributeList(playerName) {
            fetch(`/rest/player/list/${playerName}`, {
                method: 'GET',
            }).then(response => response.json())
                .then(data => {
                    if (data.code === 0) {
                        return data.data;
                    } else {
                        this.fadeInfoShow(data.msg);
                    }
                }).catch(error => {
                    this.fadeInfoShow(`请求出错: ${error}`);
                });
        },
    },
};


</script>
  
<style scoped></style>