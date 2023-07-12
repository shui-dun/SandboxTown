<template>
    <GridPanel title="🏪 购买商品" :items="items" :labels="labels"
        @clickGridItem="onClickItem" />
    <StoreItemBuyDetail v-if="showStoreItemBuyDetail" :storeId="storeId" :itemType="selectedItem.id" @onBuy="onBuy" @onCancel="cancel" />
</template>
<script>
import mixin from '@/js/mixin';
import GridPanel from './GridPanel.vue';
import StoreItemBuyDetail from './StoreItemBuyDetail.vue';

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
            items: [
            ],
            labels: [
                { 'name': 'ALL', 'prompt': '全部'},
                { 'name': 'FOOD', 'prompt': '食品' },
                { 'name': 'USABLE', 'prompt': '用品' },
                { 'name': 'WEAPON', 'prompt': '武器' },
                { 'name': 'EQUIPMENT', 'prompt': '装备' },
                { 'name': 'OTHER', 'prompt': '其他' },
            ],
            // 选择的物品
            selectedItem: null,
            // 想要买入的数目
            willingNumber: 0,
        };
    },
    async mounted() {
        // 获得商品列表
        await mixin.myGET('/rest/store/listByStore',
            new URLSearchParams({
                store: this.storeId,
            }),
        ).then((goods) => {
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
                this.items.push(item);
            });
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
