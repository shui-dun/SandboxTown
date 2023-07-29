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
        // 每隔一段时间刷新一下效果列表
        setInterval(() => {
            mixin.myGET("/rest/sprite/listMine", null, (data) => {
                this.effects = data.effects;
                // 添加图片
                this.effects.forEach((effect) => {
                    effect.img = require("@/assets/img/" + effect.effect + ".png");
                });
            });
        }, 10000);
        emitter.on("SPRITE_EFFECT_CHANGE", (data) => {
            this.effects = data;
            // 添加图片
            this.effects.forEach((effect) => {
                effect.img = require("@/assets/img/" + effect.effect + ".png");
            });
        });
    },
};
</script>

<style scoped>
.effect-list {
    position: fixed;
    top: 2%;
    left: 1%;
    display: flex;
}

/* 中间的空隙 */
.effect-item:not(:last-child) {
    margin-right: 10px;
}
</style>