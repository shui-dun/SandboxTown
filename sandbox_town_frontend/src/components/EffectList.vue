<template>
    <div class="effect-list">
        <CircleTimer class="effect-item" v-for="effect in effects" :key="effect.effect" :title="effect.effectObj.name"
            :durationMills="effect.duration * 1000" :endTimeMills="effect.expire" :image="effect.img"
            :onComplete="onComplete" :onClick="onClick" :size="60" :description="effect.effectObj.description" />
    </div>
</template>

<script>
import CircleTimer from "./CircleTimer.vue";
import mixin from "@/js/mixin";
import emitter from "@/js/mitt";

export default {
    components: {
        CircleTimer,
    },
    methods: {
        onComplete() {
        },
        onClick() {
        },
    },
    data() {
        return {
            effects: [],
        };
    },
    mounted() {
        // this.effects = [
        //     {
        //         "sprite": "USER",
        //         "effect": "FLAME_BODY",
        //         "effectObj": {
        //             "id": "FLAME_BODY",
        //             "name": "火焰之躯",
        //             "description": "你的身体被火焰包围",
        //         },
        //         "img": require("@/assets/img/FLAME_BODY.png"),
        //         "duration": 60,
        //         "expire": new Date().getTime() + 1000 * 60,
        //     },
        // ]
        // mixin.myGET("/rest/sprite/listMyEffects", null, (data) => {
        //     this.effects = data;
        // });
        emitter.on("SPRITE_EFFECT_CHANGE", (data) => {
            this.effects = data;
        });
    },
};
</script>

<style scoped>
.effect-list {
    position: fixed;
    top: 2%;
    left: 1%;
    display:flex;
}

/* 中间的空隙 */
.effect-item:not(:last-child) {
    margin-right: 10px;
}
</style>