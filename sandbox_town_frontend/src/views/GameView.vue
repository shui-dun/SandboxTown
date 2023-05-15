<template>
    <BackpackWindow v-if="backpackOpened" @close="closeBackpack" @mousedown="preventMousedownPropagation"></BackpackWindow>
    <game-canvas @itemClicked="bar" @showFadeInfo="fadeInfoShow"></game-canvas>
    <FloatingButton @click="clickBackpack" @mousedown="preventMousedownPropagation" />
    <FadeInfo :message="fadeInfoMsg" ref="fadeInfo" />
</template>

<script>
import GameCanvas from '@/components/GameCanvas.vue';
import FloatingButton from '@/components/FloatingButton.vue';
import BackpackWindow from '@/components/BackpackWindow.vue';
import FadeInfo from '@/components/FadeInfo.vue';


export default {
    components: {
        GameCanvas,
        FloatingButton,
        BackpackWindow,
        FadeInfo,
    },
    props: {
    },
    data() {
        return {
            fadeInfoMsg: '',
            backpackOpened: false,
        };
    },
    methods: {
        bar(a, b) {
            alert(`itemClicked!${a},${b}`);
        },
        clickBackpack() {
            this.backpackOpened = true;
        },
        closeBackpack() {
            this.backpackOpened = false;
        },
        preventMousedownPropagation(event) {
            event.stopPropagation();
        },
        fadeInfoShow(msg) {
            this.fadeInfoMsg = msg;
            this.$refs.fadeInfo.showAlert();
        },
    },
    computed: {
    },
    mounted() {
    }
};
</script>