<template>
    <div class="effect-list">
        <CircleTimer class="effect-item" v-for="(effect, id) in effects" :key="effect.effect" :title="effect.effectObj.name" :id="id"
            :durationMills="effect.duration * 1000" :endTimeMills="effect.expire" :image="effect.img"
            @onComplete="onComplete" @onClick="onClick" :size="60" :description="effect.effectObj.description" />
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
        onComplete(id) {
            delete this.effects[id];
        },
        onClick() {
        },
        refresh(newEffects) {
            this.effects = {};
            for (let i = 0; i < newEffects.length; i++) {
                let effect = newEffects[i];
                effect.img = require("@/assets/img/" + effect.effect + ".png");
                this.effects[effect.effect] = effect;
            }
        }
    },
    data() {
        return {
            // 效果ID和效果对象的映射
            effects: {}
        };
    },
    mounted() {
        const refreshFromBackend = () => {
            mixin.myGET("/rest/sprite/listMine", null, (data) => {
                this.refresh(data.effects);
            });
        };
        refreshFromBackend();
        // 每隔一段时间刷新一下效果列表
        setInterval(() => {
            refreshFromBackend();
        }, 10000);
        // 监听效果变化
        emitter.on("SPRITE_EFFECT_CHANGE", (data) => {
            this.refresh(data);
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