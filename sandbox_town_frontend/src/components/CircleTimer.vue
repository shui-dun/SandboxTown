<template>
    <div class="circle-timer" @click="onClick">
        <svg :width="size" :height="size">
            <circle :r="radius" :cx="size / 2" :cy="size / 2" fill="white" :stroke="'#8bd1ef'" :stroke-width="strokeWidth"
                :stroke-dasharray="circumference" :stroke-dashoffset="0" />
            <circle :r="radius" :cx="size / 2" :cy="size / 2" fill="transparent" :stroke="'black'" :stroke-width="strokeWidth"
                :stroke-dasharray="circumference" :stroke-dashoffset="offset" />
            <image :href="image" :width="size - strokeWidth * 2" :height="size - strokeWidth * 2" :x="strokeWidth"
                :y="strokeWidth" />
        </svg>
        <div class="my-tip">
            <p>{{ title }} &nbsp;&nbsp; {{ remainTime }}s </p>
            <p v-if="description.length > 0"> {{ description }} </p>
        </div>
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
            default: 5,
        },
        title: {
            type: String,
            default: "",
        },
        description: {
            type: String,
            default: "",
        },
    },
    data() {
        return {
            // 半径
            radius: this.size / 2 - this.strokeWidth,
            // 周长
            circumference: 2 * Math.PI * (this.size / 2 - this.strokeWidth),
            offset: 0,
            remainTime: 0,
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
            // 更新剩余时间
            this.calcRemain();
        }, 1000);
    },
    methods: {
        onComplete() {
            this.$emit("onComplete");
        },
        onClick() {
            this.$emit("onClick");
        },
        calcOffset() {
            const toBeElapsed = this.endTimeMills - new Date().getTime();
            const progress = Math.max(toBeElapsed / this.durationMills, 0);
            return this.circumference * (1 - progress);
        },
        calcRemain() {
            const toBeElapsed = this.endTimeMills - new Date().getTime();
            this.remainTime = Math.max(toBeElapsed / 1000, 0).toFixed(0);
        },
    },
};
</script>
  
<style scoped>
.circle-timer {
    cursor: pointer;
}

.my-tip {
    display: none;
    z-index: 101;
}

.circle-timer:hover .my-tip {
    display: block;
    /* 默认依据父级的坐标原始点为原始点 */
    position: absolute;
    top: 10px;
    left: 40px;
    background-color: #f9f9f9;
    border: 1px solid #ccc;
    border-radius: 4px;
    padding: 8px;
    font-size: 18px;
    min-width: 130px;
    max-width: 300px;
}
</style>
  