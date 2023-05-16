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
            this.$emit('showFadeInfo', event.msg);
        });

        this.game.events.on('showInfoModal', (event) => {
            this.$emit('showInfoModal', event.msg);
        });

        this.game.events.on('showAttributeList', (event) => {
            this.$emit('showAttributeList', event.itemID);
        });

        this.game.events.on('clickStore', (event) => {
            this.$emit('showStore', event.storeID);
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
    },
};


</script>
  
<style scoped></style>