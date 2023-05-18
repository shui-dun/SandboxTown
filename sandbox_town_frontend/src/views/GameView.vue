<template>
    <game-canvas @showAttributeList="attributeListShow" @showStore="storeShow" @showFadeInfo="fadeInfoShow"></game-canvas>
    <BackpackWindow v-if="backpackOpened" @close="closeBackpack" @mousedown="preventMousedownPropagation"
        @info="$refs.fadeInfo.showInfo($event)"></BackpackWindow>
    <AttributeList v-if="attributeListOpened" @close="closeAttributeList" @mousedown="preventMousedownPropagation">
    </AttributeList>
    <StorePannel v-if="storeOpened" @trade="$refs.fadeInfo.showInfo($event)" @close="closeStore"
        @mousedown="preventMousedownPropagation"></StorePannel>
    <FloatingButton @click="clickBackpack" @mousedown="preventMousedownPropagation" />
    <FadeInfo ref="fadeInfo" />
</template>

<script>
import GameCanvas from '@/components/GameCanvas.vue';
import FloatingButton from '@/components/FloatingButton.vue';
import BackpackWindow from '@/components/BackpackWindow.vue';
import FadeInfo from '@/components/FadeInfo.vue';
import AttributeList from '@/components/AttributeList.vue';
import StorePannel from '@/components/StorePannel.vue';


export default {
    components: {
        GameCanvas,
        FloatingButton,
        BackpackWindow,
        FadeInfo,
        AttributeList,
        StorePannel
    },
    props: {
    },
    data() {
        return {
            fadeInfoMsg: '',
            backpackOpened: false,
            attributeListOpened: false,
            storeOpened: false,
        };
    },
    methods: {
        clickBackpack() {
            this.backpackOpened = true;
        },
        closeBackpack() {
            this.backpackOpened = false;
        },

        attributeListShow(itemID) {
            this.attributeListOpened = true;
        },
        closeAttributeList() {
            this.attributeListOpened = false;
        },

        storeShow(storeID) {
            this.storeOpened = true;
        },
        closeStore() {
            this.storeOpened = false;
        },

        preventMousedownPropagation(event) {
            event.stopPropagation();
        },
        fadeInfoShow(msg) {
            this.$refs.fadeInfo.showInfo(msg);
        },
    },
    computed: {
    },
    mounted() {
    }
};
</script>