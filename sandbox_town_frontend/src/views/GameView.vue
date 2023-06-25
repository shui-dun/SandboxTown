<template>
    <GameCanvas @showAttributePannel="attributeListShow" @showStore="storeShow" @processBarShow="onProcessBarShow($event)" />
    <MyInfoPannel v-if="myInfoPannelOpened" @close="closeMyInfoPannel" @mousedown="preventMousedownPropagation" />
    <AttributePannel v-if="attributeListOpened" :itemName="itemNameOfAttributePannel" @close="closeAttributePannel"
        @mousedown="preventMousedownPropagation">
    </AttributePannel>
    <StorePannel v-if="storeOpened" @close="closeStore" @mousedown="preventMousedownPropagation" :storeId="currentStoreID"></StorePannel>
    <FloatingButton @click="clickBackpack" @mousedown="preventMousedownPropagation" />
    <FadeInfo ref="fadeInfo" />
    <ProcessBar v-if="showProcessBar" :duration="processBarDuration" :text="processBarText"
        :progressCompleteEvent="EventOfProgressComplete" @progressComplete="onProgressComplete" />
</template>

<script>
import GameCanvas from '@/components/GameCanvas.vue';
import FloatingButton from '@/components/FloatingButton.vue';
import MyInfoPannel from '@/components/MyInfoPannel.vue';
import FadeInfo from '@/components/FadeInfo.vue';
import AttributePannel from '@/components/AttributePannel.vue';
import StorePannel from '@/components/StorePannel.vue';
import ProcessBar from '@/components/ProcessBar.vue';
import myUtils from "@/js/myUtils.js";


export default {
    provide() {
        return {
            fadeInfoShow: this.fadeInfoShow,
        };
    },
    components: {
        GameCanvas,
        FloatingButton,
        MyInfoPannel,
        FadeInfo,
        AttributePannel,
        StorePannel,
        ProcessBar
    },
    props: {
    },
    data() {
        return {
            fadeInfoMsg: '',
            myInfoPannelOpened: false,
            attributeListOpened: false,
            storeOpened: false,
            // 进度条相关设置
            showProcessBar: false,
            processBarDuration: 5,
            processBarText: '加载中...',
            EventOfProgressComplete: () => { },
            itemNameOfAttributePannel: '',
            currentStoreID: '',
        };
    },
    methods: {
        fadeInfoShow(msg) {
            this.$refs.fadeInfo.showInfo(msg);
        },
        clickBackpack() {
            this.myInfoPannelOpened = true;
        },
        closeMyInfoPannel() {
            this.myInfoPannelOpened = false;
        },

        attributeListShow(itemID) {
            this.itemNameOfAttributePannel = itemID;
            this.attributeListOpened = true;
        },
        closeAttributePannel() {
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
        // 设置全局的工具类里面的fadeInfoShow方法
        myUtils.setFadeInfoShow(this.fadeInfoShow);

        // 向后端发送请求，检查是否登录
        let username = await myUtils.myGET('/rest/user/getUsername');
        if (username == null) {
            // 未登录，跳转到登录页面
            this.$router.push('/');
        }
    }
};
</script>