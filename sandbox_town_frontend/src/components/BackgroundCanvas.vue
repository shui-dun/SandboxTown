<template>
    <div class="player-info-wrapper">
        <div class="player-info">
            <button class="close-btn" @click="closeComponent">×</button>
        </div>
    </div>
</template>

<script>
export default {
    components: {
    },
    data() {
        return {};
    },
    mounted() {
    },
    computed: {
    },
    methods: {
        closeComponent() {
            this.$emit('close');
        },
    },
};
</script>

<style scoped>
.player-info-wrapper {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 9999;
    pointer-events: auto;
}

.player-info {
    background-color: #fff;
    border-radius: 5px;
    padding: 1rem;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}

.close-btn {
    position: absolute;
    top: 0px;
    right: 10px;
    background: none;
    border: none;
    font-size: 3.5rem;
    cursor: pointer;
}

</style>
