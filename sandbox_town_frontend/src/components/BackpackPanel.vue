<template>
    <div>
        <GridPanel title="🎒 背包" :items="items" :labels="labels" @clickGridItem="onClickBackpackItem" />
        <ItemDetail v-if="showItemDetail" :itemId="selectedItem.id" @onConfirm="confirm" @onCancel="cancel" />
    </div>
</template>

<script>
import GridPanel from './GridPanel.vue';
import ItemDetail from './ItemDetail.vue';
import mixin from '@/js/mixin.js';
import { ITEM_LABELS } from '@/js/constants.js';

export default {
    components: {
        GridPanel,
        ItemDetail,
    },
    data() {
        return {
            items: [],
            labels: ITEM_LABELS,
            showItemDetail: false,
            // 选择的物品
            selectedItem: null,
        };
    },
    mounted() {
        this.flush();
    },
    computed: {
    },
    methods: {
        confirm(event) {
            // 刷新背包
            this.flush();
            this.showItemDetail = false;
        },
        cancel() {
            this.showItemDetail = false;
        },
        onClickBackpackItem(item) {
            this.selectedItem = item;
            this.showItemDetail = true;
        },
        flush() {
            // 从后端获取玩家物品信息
            mixin.myGET('/rest/item/listMyItemsInBackpack', null, (data) => {
                let itemLst = [];
                // 重命名物品的属性名
                data.forEach((element) => {
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
    },
};
</script>

<style scoped></style>
