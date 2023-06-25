<template>
    <div>
        <GridPannel ref="gridPannel" title="🎒 物品栏" :items="items" :labels="labels" @clickGridItem="onClickBackpackItem($event)" />
        <InquiryPanel v-if="showInquiryPanel" :prompt="inquiryPanelPrompt" @onConfirm="confirm" @onCancel="cancel" />
    </div>
</template>

<script>
import GridPannel from './GridPannel.vue';
import InquiryPanel from './InquiryPanel.vue';
import myUtils from '@/js/myUtils.js';

export default {
    inject: ['fadeInfoShow'],
    components: {
        GridPannel,
        InquiryPanel,
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
            showInquiryPanel: false,
            // 选择的物品
            selectedItem: null,
            inquiryPanelPrompt: '',
        };
    },
    mounted() {
        // 从后端获取玩家物品信息
        myUtils.myGET('/rest/item/listMine', null, (data) => {
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
            this.$refs.gridPannel.filterItems();
        });
    },
    computed: {
    },
    methods: {
        confirm() {
            // if (this.selectedItem.category === 'item') {
            //     this.selectedItem.caption.num -= 1;
            //     // 使用物品
            //     myUtils.myPOST(
            //         '/rest/item/use',
            //         new URLSearchParams({
            //             itemId: this.selectedItem.id,
            //         }),
            //         (data) => {
            //             // 显示提示信息
            //         },
            //     )
            // } else if (this.selectedItem.category === 'equipment') {
            //     this.selectedItem.caption.num -= 1;
            //     this.fadeInfoShow(`装备${this.selectedItem.name}`)
            // } else if (this.selectedItem.category === 'pet') {
            //     this.selectedItem.caption.num -= 1;
            //     this.fadeInfoShow(`放置${this.selectedItem.name}`)
            // } else if (this.selectedItem.category === 'architecture') {
            //     // 先要进行放置操作
            //     this.selectedItem.caption.num -= 1;
            //     this.fadeInfoShow(`放置${this.selectedItem.name}`)
            // }
            this.showInquiryPanel = false;
        },
        cancel() {
            this.showInquiryPanel = false;
        },
        onClickBackpackItem(item) {
            // // 如果物品不可使用，直接返回
            // if (!item.usable) {
            //     this.fadeInfoShow(`不能使用${item.name}`);
            //     return;
            // }
            // this.selectedItem = item;
            // if (item.caption.num <= 0) {
            //     this.fadeInfoShow(`你没有${this.selectedItem.name}`)
            //     return;
            // }
            // if (item.category === 'item') {
            //     this.inquiryPanelPrompt = '确定使用' + item.name + '吗？';
            // } else if (item.category === 'equipment') {
            //     this.inquiryPanelPrompt = '确定装备' + item.name + '吗？';
            // } else if (item.category === 'pet') {
            //     this.inquiryPanelPrompt = '确定放置' + item.name + '吗？';
            // } else if (item.category === 'architecture') {
            //     this.inquiryPanelPrompt = '确定放置' + item.name + '吗？';
            // } else {
            //     return;
            // }
            this.showInquiryPanel = true;
        },
    },
};
</script>

<style scoped></style>
