<template>
    <GridPanel title="🏬 售卖商品" :items="items" :labels="labels"
        @clickGridItem="onClickItem" />
    <StoreItemSoldDetail v-if="showStoreItemSoldDetail" :storeId="storeId" :itemId="selectedItem.id" @onSold="onSold" @onCancel="cancel" />
</template>
<script>
import mixin from '@/js/mixin';
import GridPanel from './GridPanel.vue';
import StoreItemSoldDetail from './StoreItemSoldDetail.vue';
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
        StoreItemSoldDetail,
    },
    data() {
        return {
            showStoreItemSoldDetail: false,
            // 用户可以买的物品
            items: [],
            labels: ITEM_LABELS,
            // 选择的物品
            selectedItem: null,
            // 想要卖出的数目
            willingNumber: 0,
        };
    },
    async mounted() {
        // 获得商品列表
        await mixin.myGET('/rest/item/listMyItemsInBackpack',
        ).then((itemsInBackpack) => {
            let itemLst = [];
            itemsInBackpack.forEach((element) => {
                let item = {};
                item.id = element.id;
                item.name = element.itemTypeObj.name;
                item.caption = { num: element.itemCount };
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
                // 将用户物品信息添加到items
                itemLst.push(item);
            });
            this.items = itemLst;
        });
    },
    methods: {
        onClickItem(item) {
            this.selectedItem = item;
            this.showStoreItemSoldDetail = true;
        },
        onSold(value) {
            this.selectedItem.content.count -= value;
            this.selectedItem.caption.num -= value;
            this.showStoreItemSoldDetail = false;
        },
        cancel() {
            this.showStoreItemSoldDetail = false;
        },
    },
}
</script>
