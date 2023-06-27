<template>
    <div>
        <GridPanel ref="gridPanel" title="🎒 物品栏" :items="items" :labels="labels" @clickGridItem="onClickBackpackItem" />
        <ItemDetail v-if="showItemDetail" :itemId="selectedItem.id" @onConfirm="confirm" @onCancel="cancel" />
    </div>
</template>

<script>
import GridPanel from './GridPanel.vue';
import ItemDetail from './ItemDetail.vue';
import myUtils from '@/js/myUtils.js';

export default {
    components: {
        GridPanel,
        ItemDetail,
    },
    data() {
        return {
            items: [
            ],
            labels: [
                { 'name': 'FOOD', 'prompt': '食品' },
                { 'name': 'USABLE', 'prompt': '用品' },
                { 'name': 'WEAPON', 'prompt': '武器' },
                { 'name': 'EQUIPMENT', 'prompt': '装备' },
            ],
            showItemDetail: false,
            // 选择的物品
            selectedItem: null,
        };
    },
    mounted() {
        // 从后端获取玩家物品信息
        myUtils.myGET('/rest/item/listMyItemsInBackpack', null, (data) => {
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
                this.items.push(item);
            });
            this.$refs.gridPanel.filterItems();
        });
    },
    computed: {
    },
    methods: {
        confirm() {
            // TODO: 减少物品数量
            this.showItemDetail = false;
        },
        cancel() {
            this.showItemDetail = false;
        },
        onClickBackpackItem(item) {
            this.selectedItem = item;
            this.showItemDetail = true;
        },
    },
};
</script>

<style scoped></style>
