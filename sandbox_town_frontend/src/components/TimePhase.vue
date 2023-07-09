<template>
    <CircleTimer :size="70" class="circle-timer" :durationMills="durationMills" :endTimeMills="endTimeMills" :image="image" :onComplete="onComplete"
        :onClick="onClick" />
</template>

<script>
// import mixin from "@/js/mixin.js";
import CircleTimer from "./CircleTimer.vue";

export default {
    components: {
        CircleTimer,
    },
    methods: {
        onComplete() {
            console.log("complete");
        },
        onClick() {
            console.log("click");
        },
    },
    data() {
        return {
            endTimeMills: Date.now() + 5 * 1000,
            durationMills: 20000,
            image: require("@/assets/img/BREAD.png"),
        };
    },
};
</script>

<style>
.circle-timer {
    position: fixed;
    bottom: 2%;
    left: 1%;
}
</style>