<template>
    <div class="progress-container" :style="{ zIndex: zIndex }">
        <div class="progress-bar" :style="{ animationDuration: duration + 's' }"></div>
        <div class="progress-text">{{ text }}</div>
    </div>
</template>
  
<script>
export default {
    name: 'TopProgressBar',
    props: {
        duration: {
            type: Number,
            required: true
        },
        text: {
            type: String,
            required: true
        },
        zIndex: {
            type: Number,
            default: 1000
        },
        progressCompleteEvent: {
            type: Function,
            required: true
        }
    },
    mounted() {
        setTimeout(() => {
            this.progressCompleteEvent();
            this.$emit('progressComplete');
        }, this.duration * 1000);
    }
}
</script>
  
  

<style scoped>
.progress-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.progress-bar {
    height: 5px;
    width: 100%;
    background-color: #4caf50;
    animation-name: progressBar;
    animation-timing-function: linear;
    animation-fill-mode: forwards;
}

.progress-text {
    margin-top: 5px;
    font-size: 14px;
    font-weight: bold;
}

@keyframes progressBar {
    0% {
        width: 0;
    }

    100% {
        width: 100%;
    }
}
</style>
