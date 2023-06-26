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
                { 'name': 'food', 'prompt': '食品' },
                { 'name': 'usable', 'prompt': '用品' },
                { 'name': 'weapon', 'prompt': '武器' },
                { 'name': 'equipment', 'prompt': '装备' },
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
                item.name = element.itemTypeBean.name;
                item.caption = { num: element.itemCount };
                item.image = require(`@/assets/img/${element.itemType}.png`);
                // 设置物品的标签
                item.labels = [];
                // 如果物品包含helmet（头盔）, chest（胸甲）, leg（腿甲）, boots（鞋）的label，将其替换为equipment（装备）
                let isEquipment = false;
                for (let label of element.labels) {
                    if ((label === 'helmet' || label === 'chest' || label === 'leg' || label === 'boots') && !isEquipment) {
                        isEquipment = true;
                        item.labels.push('equipment');
                    } else {
                        item.labels.push(label);
                    }
                }
                item.description = element.itemTypeBean.description;
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
