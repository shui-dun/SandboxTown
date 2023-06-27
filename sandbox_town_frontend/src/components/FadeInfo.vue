<template>
    <div class="fade-info-wrapper">
        <transition name="fade" v-for="(itemVal, itemKey) in messages" :key="itemKey">
            <div v-if="show[itemKey]" class="fade-info-alert">
                <div class="fade-info-edge"></div>
                <div class="alert-content">{{ itemVal }}</div>
                <div class="fade-info-edge"></div>
            </div>
        </transition>
    </div>
</template>
  
<script>
const { v4: uuidv4 } = require('uuid');

export default {
    data() {
        return {
            messages: {},
            show: {}
        };
    },
    mounted() { },
    methods: {
        showInfo(msg) {
            const uuid = uuidv4();
            this.messages[uuid] = msg;
            this.show[uuid] = true;
            setTimeout(() => {
                this.show[uuid] = false;
                setTimeout(() => {
                    delete this.messages[uuid];
                    delete this.show[uuid];
                }, 800);
            }, 2500);
        },
    },
};
</script>
  
<style scoped>
.fade-info-wrapper {
    display: flex;
    position: fixed;
    bottom: 2%;
    left: 50%;
    flex-direction: column;
    align-items: flex-start;
    /* 使子元素根据其内容自适应宽度，而不是填满容器 */
    z-index: 300;
}

.fade-info-alert {
    /* display: flex; */
    flex-grow: 0;
    margin: 5px;
    transform: translateX(-50%);
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    border-radius: 5px;
    padding: 10px;
}

.fade-info-edge {
    width: 10px;
    height: 100%;
    background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 10 100" preserveAspectRatio="none"><path d="M0,0 Q5,50 10,0 Q5,50 0,100" fill="%23f5c6cb"/></svg>');
}

.alert-content {
    padding: 0 10px;
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.5s;
}

.fade-enter,
.fade-leave-to {
    opacity: 0;
}
</style>
  