<template>
    <game-canvas @itemClicked="bar"></game-canvas>
    <FloatingButton @click="clickBackpack" />
    <div v-if="backpackOpened"></div>
</template>

<script>
import GameCanvas from '@/components/GameCanvas.vue';
import FloatingButton from '@/components/FloatingButton.vue';


export default {
    components: {
        GameCanvas,
        FloatingButton,
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
        }
    },
    computed: {
    },
    mounted() {
    }
};
</script>