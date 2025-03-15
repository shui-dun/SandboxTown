<template>
    <GridPanel title="🏪 购买商品" :items="items" :labels="labels"
        @clickGridItem="onClickItem" />
    <StoreItemBuyDetail v-if="showStoreItemBuyDetail" :storeId="storeId" :itemType="selectedItem.id" @onBuy="onBuy" @onCancel="cancel" />
</template>
<script>
import mixin from '@/js/mixin';
import GridPanel from './GridPanel.vue';
import StoreItemBuyDetail from './StoreItemBuyDetail.vue';
import { ITEM_LABELS } from '@/js/constants.js';

export default {
    props: {
        storeId: {
            type: String,
            required: true,
        },
    },
    components: {
        GridPanel,
        StoreItemBuyDetail,
    },
    data() {
        return {
            showStoreItemBuyDetail: false,
            // 用户可以买的物品
            items: [],
            labels: ITEM_LABELS,
            // 选择的物品
            selectedItem: null,
            // 想要买入的数目
            willingNumber: 0,
        };
    },
    mounted() {
        // 获得商品列表
        mixin.myGET('/rest/store/listByStore',
            new URLSearchParams({
                store: this.storeId,
            }),
        ).then((goods) => {
            let itemLst = [];
            goods.forEach((element) => {
                let item = {};
                item.id = element.itemType;
                item.name = element.itemTypeObj.name;
                item.caption = { price: '￥' + element.price, num: element.count };
                item.image = require(`@/assets/img/${element.itemType}.png`);
                // 设置物品的标签
                item.labels = [];
                // 如果物品包含HELMET（头盔）, CHEST（胸甲）, LEG（腿甲）, BOOTS（鞋）的LABEL，将其替换为EQUIPMENT（装备）
                let isEquipment = false;
                for (let label of element.itemTypeObj.labels) {
                    if ((label === 'HELMET' || label === 'CHEST' || label === 'LEG' || label === 'BOOTS') && !isEquipment) {
                        isEquipment = true;
                        item.labels.push('EQUIPMENT');
                    } else {
                        item.labels.push(label);
                    }
                }
                item.description = element.itemTypeObj.description;
                item.content = element;                
                itemLst.push(item);
            });
            // 好像只有通过为this.items赋值才能触发GridPanel的更新（this.$watch），而通过this.items.push()不行
            this.items = itemLst;
        });
    },
    methods: {
        onClickItem(item) {
            this.selectedItem = item;
            this.showStoreItemBuyDetail = true;
        },
        async onBuy(value) {
            this.selectedItem.content.count -= value;
            this.selectedItem.caption.num -= value;
            this.showStoreItemBuyDetail = false;
        },
        cancel() {
            this.showStoreItemBuyDetail = false;
        },
    },
}
</script>
