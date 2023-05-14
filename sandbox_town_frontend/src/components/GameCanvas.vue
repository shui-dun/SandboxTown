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
            scene: [mainScene, storeScene],
        };

        this.game = new Phaser.Game(config);

        this.game.events.on('itemClicked', () => {
            this.$emit('itemClicked');
        });


        // 测试websocket
        var ws = new WebSocket("ws://localhost:9090/event");

        ws.onopen = function () {
            console.log("Connection open ...");
            ws.send("Hello WebSockets!");
        };

        ws.onmessage = function (event) {
            if (typeof event.data === String) {
                console.log("Received data string");
            }
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