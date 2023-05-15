<template>
    <BackpackWindow v-if="backpackOpened" @close="closeBackpack" @mousedown="preventMousedownPropagation"></BackpackWindow>
    <game-canvas @itemClicked="bar" @showFadeInfo="fadeInfoShow" @showInfoModal="infoModalShow"></game-canvas>
    <FloatingButton @click="clickBackpack" @mousedown="preventMousedownPropagation" />
    <FadeInfo ref="fadeInfo" />
    <InfoModal ref="infoModal" @mousedown="preventMousedownPropagation"/>
</template>

<script>
import GameCanvas from '@/components/GameCanvas.vue';
import FloatingButton from '@/components/FloatingButton.vue';
import BackpackWindow from '@/components/BackpackWindow.vue';
import FadeInfo from '@/components/FadeInfo.vue';
import InfoModal from '@/components/InfoModal.vue';


export default {
    components: {
        GameCanvas,
        FloatingButton,
        BackpackWindow,
        FadeInfo,
        InfoModal,
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
            this.$refs.fadeInfo.showInfo(msg);
        },
        infoModalShow(msg) {
            this.$refs.infoModal.showInfo(msg);
        },
    },
    computed: {
    },
    mounted() {
    }
};
</script>