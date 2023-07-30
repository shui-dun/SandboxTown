<template>
    <CircleTimer :title="title" :size="70" class="circle-timer" :durationMills="durationMills" :endTimeMills="endTimeMills"
        :image="image" @onComplete="onComplete" @onClick="onClick" />
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
        updateTimePhase(timePhase) {
            if (timePhase.timeFrame == "DAY") {
                this.title = "白天";
                this.image = require("@/assets/img/DAY.png");
            } else if (timePhase.timeFrame == "DUSK") {
                this.title = "黄昏";
                this.image = require("@/assets/img/DUSK.png");
            } else if (timePhase.timeFrame == "NIGHT") {
                this.title = "夜晚";
                this.image = require("@/assets/img/NIGHT.png");
            } else if (timePhase.timeFrame == "DAWN") {
                this.title = "黎明";
                this.image = require("@/assets/img/DAWN.png");
            }
            this.durationMills = timePhase.timeFrameDuration;
            this.endTimeMills = timePhase.timeFrameEndTime;
        },
    },
    data() {
        return {
            endTimeMills: 0,
            durationMills: 0,
            image: require("@/assets/img/DAY.png"),
            title: "白天",
        };
    },
    mounted() {
        mixin.myGET("/rest/time/getTimeFrame", null, (data) => {
            this.updateTimePhase(data);
        });
        emitter.on("TIME_FRAME_NOTIFY", (data) => {
            this.updateTimePhase(data);
        });
    },
};
</script>

<style scoped>
.circle-timer {
    position: fixed;
    bottom: 2%;
    left: 1%;
}
</style>