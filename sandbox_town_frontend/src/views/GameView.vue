<template>
    <GameCanvas @showSpritePanel="attributeListShow" @showStore="storeShow" @processBarShow="onProcessBarShow($event)" />
    <MyInfoPanel v-if="myInfoPanelOpened" @close="closeMyInfoPanel" @mousedown="preventMousedownPropagation" />
    <SpritePanel v-if="attributeListOpened" :itemName="itemNameOfSpritePanel" @close="closeSpritePanel"
        @mousedown="preventMousedownPropagation">
    </SpritePanel>
    <StorePanel v-if="storeOpened" @close="closeStore" @mousedown="preventMousedownPropagation" :storeId="currentStoreID"></StorePanel>
    <FloatingButton @click="clickBackpack" @mousedown="preventMousedownPropagation" />
    <ProcessBar v-if="showProcessBar" :duration="processBarDuration" :text="processBarText"
        :progressCompleteEvent="EventOfProgressComplete" @progressComplete="onProgressComplete" />
</template>

<script>
import GameCanvas from '@/components/GameCanvas.vue';
import FloatingButton from '@/components/FloatingButton.vue';
import MyInfoPanel from '@/components/MyInfoPanel.vue';
import SpritePanel from '@/components/SpritePanel.vue';
import StorePanel from '@/components/StorePanel.vue';
import ProcessBar from '@/components/ProcessBar.vue';
import mixin from "@/js/mixin.js";


export default {
    components: {
        GameCanvas,
        FloatingButton,
        MyInfoPanel,
        SpritePanel,
        StorePanel,
        ProcessBar
    },
    props: {
    },
    data() {
        return {
            myInfoPanelOpened: false,
            attributeListOpened: false,
            storeOpened: false,
            // 进度条相关设置
            showProcessBar: false,
            processBarDuration: 5,
            processBarText: '加载中...',
            EventOfProgressComplete: () => { },
            itemNameOfSpritePanel: '',
            currentStoreID: '',
        };
    },
    methods: {
        clickBackpack() {
            this.myInfoPanelOpened = true;
        },
        closeMyInfoPanel() {
            this.myInfoPanelOpened = false;
        },

        attributeListShow(itemID) {
            this.itemNameOfSpritePanel = itemID;
            this.attributeListOpened = true;
        },
        closeSpritePanel() {
            this.attributeListOpened = false;
        },

        storeShow(storeID) {
            this.storeOpened = true;
            this.currentStoreID = storeID;
        },
        closeStore() {
            this.storeOpened = false;
        },

        preventMousedownPropagation(event) {
            event.stopPropagation();
        },
        onProgressComplete() {
            this.showProcessBar = false;
        },
        onProcessBarShow(event) {
            this.processBarDuration = event.duration;
            this.processBarText = event.text;
            this.EventOfProgressComplete = event.progressCompleteEvent;
            this.showProcessBar = true;
        }
    },
    computed: {
    },
    async mounted() {
        // 向后端发送请求，检查是否登录
        let username = await mixin.myGET('/rest/user/getUsername');
        if (username == null) {
            // 未登录，跳转到登录页面
            this.$router.push('/');
        }
    }
};
</script>