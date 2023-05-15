<template>
    <transition name="fade">
        <div v-if="show" class="torn-paper-alert">
            <div class="torn-paper-edge"></div>
            <div class="alert-content">{{ message }}</div>
            <div class="torn-paper-edge"></div>
        </div>
    </transition>
</template>
  
<script>
export default {
    props: {
        message: {type: String},
    },
    data() {
        return {
            show: false,
        };
    },
    mounted() {},
    methods: {
        showAlert() {
            this.show = true;
            setTimeout(() => {
                this.show = false;
            }, 3000);
        },
    },
};
</script>
  
<style scoped>
.torn-paper-alert {
    display: flex;
    position: fixed;
    bottom: 10%;
    left: 50%;
    transform: translateX(-50%);
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    border-radius: 5px;
    padding: 10px;
    z-index: 1000;
}

.torn-paper-edge {
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
  