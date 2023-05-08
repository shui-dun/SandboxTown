<template>
    <game-canvas @itemClicked="bar"></game-canvas>
</template>

<script>
import GameCanvas from '@/components/GameCanvas.vue';

export default {
    components: {
        GameCanvas,
    },
    props: {
    },
    data() {
        return {
        };
    },
    methods: {
        bar() {
            alert("itemClicked!");
        }
    },
    computed: {
    },
    mounted() {
    }
};
</script>