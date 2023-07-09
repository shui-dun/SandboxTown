<template>
    <div class="circle-timer" @click="onClick">
        <svg :width="size" :height="size">
            <circle :r="radius" :cx="size / 2" :cy="size / 2" fill="white" :stroke="'#8bd1ef'" :stroke-width="strokeWidth"
                :stroke-dasharray="circumference" :stroke-dashoffset="0" />
            <circle :r="radius" :cx="size / 2" :cy="size / 2" fill="white" :stroke="'black'" :stroke-width="strokeWidth"
                :stroke-dasharray="circumference" :stroke-dashoffset="offset" />
            <image :href="image" :width="size - strokeWidth * 2" :height="size - strokeWidth * 2" :x="strokeWidth"
                :y="strokeWidth" />
        </svg>
    </div>
</template>
  
<script>
export default {
    props: {
        durationMills: Number,
        endTimeMills: Number,
        image: String,
        size: {
            type: Number,
            default: 70,
        },
        strokeWidth: {
            type: Number,
            default: 7,
        },
    },
    data() {
        return {
            // 半径
            radius: this.size / 2 - this.strokeWidth,
            // 周长
            circumference: 2 * Math.PI * (this.size / 2 - this.strokeWidth),
            offset: 0,
        };
    },
    mounted() {
        this.offset = this.calcOffset();
        setInterval(() => {
            // 更新进度条比例
            this.offset = this.calcOffset();
            // 判断是否结束
            if (Date.now() >= this.endTimeMills) {
                this.onComplete();
            }
        }, 1000);
    },
    methods: {
        onComplete() {
            this.$emit("onComplete");
        },
        onClick() {
            this.$emit("onClick");
            alert("click");
        },
        calcOffset() {
            const toBeElapsed = this.endTimeMills - new Date().getTime();
            const progress = Math.max(toBeElapsed / this.durationMills, 0);
            return this.circumference * (1 - progress);
        }
    },
};
</script>
  
<style scoped>
.circle-timer {
    cursor: pointer;
}
</style>
  