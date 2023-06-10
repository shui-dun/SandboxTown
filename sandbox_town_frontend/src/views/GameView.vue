<template>
    <gameCanvas @showAttributeList="attributeListShow" @showStore="storeShow" @processBarShow="onProcessBarShow($event)" />
    <BackpackWindow v-if="backpackOpened" @close="closeBackpack" @mousedown="preventMousedownPropagation" />
    <AttributeList v-if="attributeListOpened" :itemName="itemNameOfAttributeList" @close="closeAttributeList"
        @mousedown="preventMousedownPropagation">
    </AttributeList>
    <StorePannel v-if="storeOpened" @close="closeStore" @mousedown="preventMousedownPropagation"></StorePannel>
    <FloatingButton @click="clickBackpack" @mousedown="preventMousedownPropagation" />
    <FadeInfo ref="fadeInfo" />
    <ProcessBar v-if="showProcessBar" :duration="processBarDuration" :text="processBarText"
        :progressCompleteEvent="EventOfProgressComplete" @progressComplete="onProgressComplete" />
</template>

<script>
import GameCanvas from '@/components/GameCanvas.vue';
import FloatingButton from '@/components/FloatingButton.vue';
import BackpackWindow from '@/components/BackpackWindow.vue';
import FadeInfo from '@/components/FadeInfo.vue';
import AttributeList from '@/components/AttributeList.vue';
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
        BackpackWindow,
        FadeInfo,
        AttributeList,
        StorePannel,
        ProcessBar
    },
    props: {
    },
    data() {
        return {
            fadeInfoMsg: '',
            backpackOpened: false,
            attributeListOpened: false,
            storeOpened: false,
            // 进度条相关设置
            showProcessBar: false,
            processBarDuration: 5,
            processBarText: '加载中...',
            EventOfProgressComplete: () => { },
            itemNameOfAttributeList: '',
        };
    },
    methods: {
        fadeInfoShow(msg) {
            this.$refs.fadeInfo.showInfo(msg);
        },
        clickBackpack() {
            this.backpackOpened = true;
        },
        closeBackpack() {
            this.backpackOpened = false;
        },

        attributeListShow(itemID) {
            this.itemNameOfAttributeList = itemID;
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
        let username = await myUtils.myFetch('/rest/user/getUsername', 'GET');
        if (username == null) {
            // 未登录，跳转到登录页面
            this.$router.push('/');
        }
    }
};
</script>