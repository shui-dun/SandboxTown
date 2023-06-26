<template>
    <GameCanvas @showAttributePanel="attributeListShow" @showStore="storeShow" @processBarShow="onProcessBarShow($event)" />
    <MyInfoPanel v-if="myInfoPanelOpened" @close="closeMyInfoPanel" @mousedown="preventMousedownPropagation" />
    <AttributePanel v-if="attributeListOpened" :itemName="itemNameOfAttributePanel" @close="closeAttributePanel"
        @mousedown="preventMousedownPropagation">
    </AttributePanel>
    <StorePanel v-if="storeOpened" @close="closeStore" @mousedown="preventMousedownPropagation" :storeId="currentStoreID"></StorePanel>
    <FloatingButton @click="clickBackpack" @mousedown="preventMousedownPropagation" />
    <ProcessBar v-if="showProcessBar" :duration="processBarDuration" :text="processBarText"
        :progressCompleteEvent="EventOfProgressComplete" @progressComplete="onProgressComplete" />
</template>

<script>
import GameCanvas from '@/components/GameCanvas.vue';
import FloatingButton from '@/components/FloatingButton.vue';
import MyInfoPanel from '@/components/MyInfoPanel.vue';
import AttributePanel from '@/components/AttributePanel.vue';
import StorePanel from '@/components/StorePanel.vue';
import ProcessBar from '@/components/ProcessBar.vue';
import myUtils from "@/js/myUtils.js";


export default {
    components: {
        GameCanvas,
        FloatingButton,
        MyInfoPanel,
        AttributePanel,
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
            itemNameOfAttributePanel: '',
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
            this.itemNameOfAttributePanel = itemID;
            this.attributeListOpened = true;
        },
        closeAttributePanel() {
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
        let username = await myUtils.myGET('/rest/user/getUsername');
        if (username == null) {
            // 未登录，跳转到登录页面
            this.$router.push('/');
        }
    }
};
</script>