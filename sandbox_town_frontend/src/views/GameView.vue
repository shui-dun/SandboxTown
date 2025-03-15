<template>
    <GameCanvas @showSpritePanel="attributeListShow" @showStore="storeShow" @showFactory="factoryShow" />
    <!-- @mousedown.stop 阻止事件冒泡，子组件中按下鼠标时不触发父组件的事件处理程序 -->
    <MyInfoPanel v-if="myInfoPanelOpened" @close="closeMyInfoPanel" @mousedown.stop />
    <SpritePanel v-if="attributeListOpened" :itemName="itemNameOfSpritePanel" @close="closeSpritePanel"
        @mousedown.stop>
    </SpritePanel>
    <ItemBar @mousedown.stop />
    <TimePhase />
    <EffectList />
    <ApplePicking @mousedown.stop />
    <StorePanel v-if="storeOpened" @close="closeStore" @mousedown.stop :storeId="currentStoreID"></StorePanel>
    <FusionPanel v-if="fusionOpened" @close="closeFactory" @mousedown.stop />
    <FloatingButton @click="clickBackpack" @mousedown.stop />
</template>

<script>
import GameCanvas from '@/components/GameCanvas.vue';
import FloatingButton from '@/components/FloatingButton.vue';
import MyInfoPanel from '@/components/MyInfoPanel.vue';
import SpritePanel from '@/components/SpritePanel.vue';
import StorePanel from '@/components/StorePanel.vue';
import FusionPanel from '@/components/FusionPanel.vue';
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
        FusionPanel,
        ItemBar,
        TimePhase,
        EffectList,
        ApplePicking,
    },
    data() {
        return {
            myInfoPanelOpened: false,
            attributeListOpened: false,
            storeOpened: false,
            fusionOpened: false,
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
        factoryShow() {
            this.fusionOpened = true;
        },
        closeFactory() {
            this.fusionOpened = false;
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