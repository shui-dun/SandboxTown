<template>
    <BackpackWindow v-if="backpackOpened" @close="closeBackpack" @mousedown="preventMousedownPropagation"></BackpackWindow>
    <game-canvas @itemClicked="bar"></game-canvas>
    <FloatingButton @click="clickBackpack" @mousedown="preventMousedownPropagation" />
</template>

<script>
import GameCanvas from '@/components/GameCanvas.vue';
import FloatingButton from '@/components/FloatingButton.vue';
import BackpackWindow from '@/components/BackpackWindow.vue';


export default {
    components: {
        GameCanvas,
        FloatingButton,
        BackpackWindow,
    },
    props: {
    },
    data() {
        return {
            backpackOpened: false,
        };
    },
    methods: {
        bar() {
            alert("itemClicked!");
        },
        clickBackpack() {
            this.backpackOpened = true;
        },
        closeBackpack() {
            this.backpackOpened = false;
        },
        preventMousedownPropagation(event) {
            event.stopPropagation();
        }
    },
    computed: {
    },
    mounted() {
    }
};
</script>