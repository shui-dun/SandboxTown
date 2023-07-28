<template>
    <GameCanvas @showSpritePanel="attributeListShow" @showStore="storeShow" />
    <MyInfoPanel v-if="myInfoPanelOpened" @close="closeMyInfoPanel" @mousedown="preventMousedownPropagation" />
    <SpritePanel v-if="attributeListOpened" :itemName="itemNameOfSpritePanel" @close="closeSpritePanel"
        @mousedown="preventMousedownPropagation">
    </SpritePanel>
    <ItemBar @mousedown="preventMousedownPropagation" />
    <TimePhase />
    <EffectList />
    <ApplePicking @mousedown="preventMousedownPropagation" />
    <StorePanel v-if="storeOpened" @close="closeStore" @mousedown="preventMousedownPropagation" :storeId="currentStoreID"></StorePanel>
    <FloatingButton @click="clickBackpack" @mousedown="preventMousedownPropagation" />
</template>

<script>
import GameCanvas from '@/components/GameCanvas.vue';
import FloatingButton from '@/components/FloatingButton.vue';
import MyInfoPanel from '@/components/MyInfoPanel.vue';
import SpritePanel from '@/components/SpritePanel.vue';
import StorePanel from '@/components/StorePanel.vue';
import ItemBar from '@/components/ItemBar.vue';
import TimePhase from '@/components/TimePhase.vue';
import EffectList from '@/components/EffectList.vue';
import ApplePicking from '@/components/ApplePicking.vue';
import mixin from "@/js/mixin.js";


export default {
    components: {
        GameCanvas,
        FloatingButton,
        MyInfoPanel,
        SpritePanel,
        StorePanel,
        ItemBar,
        TimePhase,
        EffectList,
        ApplePicking,
    },
    props: {
    },
    data() {
        return {
            myInfoPanelOpened: false,
            attributeListOpened: false,
            storeOpened: false,
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