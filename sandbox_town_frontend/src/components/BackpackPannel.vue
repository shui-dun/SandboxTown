<template>
    <div>
        <GridPannel title="🎒 物品栏" :items="items" :categories="categories" @clickGridItem="onClickBackpackItem($event)" />
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
            player: {},
            items: [
                // { id: 1, name: '面包', image: require("@/assets/img/bread.png"), category: 'item', description: '具有松软的质地和微甜的口感', extra: { num: 1 } },            ],
            ],
            categories: [
                { 'label': 'item', 'prompt': '物品' },
                { 'label': 'equipment', 'prompt': '装备' },
                { 'label': 'pet', 'prompt': '宠物' },
                { 'label': 'architecture', 'prompt': '建筑' },
            ],
            showInquiryPanel: false,
            // 选择的物品
            selectedItem: null,
        };
    },
    mounted() {
        // 从后端获取玩家物品信息
        myUtils.myGET('/rest/item/listMine', null, (data) => {
            // 重命名物品的属性名
            data.forEach((item) => {
                item.extra = { num: item.itemCount };
                item.category = 'item';
                item.image = require(`@/assets/img/${item.id}.png`);
            });
            // 将用户物品信息添加到items最后
            this.items.push(...data);
        });
    },
    computed: {
    },
    methods: {
        confirm() {
            if (this.selectedItem.category === 'item') {
                this.selectedItem.extra.num -= 1;
                // 使用物品
                myUtils.myPOST(
                    '/rest/item/use',
                    new URLSearchParams({
                        itemId: this.selectedItem.id,
                    }),
                    (data) => {
                        // 显示提示信息
                    },
                )
            } else if (this.selectedItem.category === 'equipment') {
                this.selectedItem.extra.num -= 1;
                this.fadeInfoShow(`装备${this.selectedItem.name}`)
            } else if (this.selectedItem.category === 'pet') {
                this.selectedItem.extra.num -= 1;
                this.fadeInfoShow(`放置${this.selectedItem.name}`)
            } else if (this.selectedItem.category === 'architecture') {
                // 先要进行放置操作
                this.selectedItem.extra.num -= 1;
                this.fadeInfoShow(`放置${this.selectedItem.name}`)
            }
            this.showInquiryPanel = false;
        },
        cancel() {
            this.showInquiryPanel = false;
        },
        onClickBackpackItem(item) {
            // 如果物品不可使用，直接返回
            if (!item.usable) {
                this.fadeInfoShow(`不能使用${item.name}`);
                return;
            }
            this.selectedItem = item;
            if (item.extra.num <= 0) {
                this.fadeInfoShow(`你没有${this.selectedItem.name}`)
                return;
            }
            if (item.category === 'item') {
                this.inquiryPanelPrompt = '确定使用' + item.name + '吗？';
            } else if (item.category === 'equipment') {
                this.inquiryPanelPrompt = '确定装备' + item.name + '吗？';
            } else if (item.category === 'pet') {
                this.inquiryPanelPrompt = '确定放置' + item.name + '吗？';
            } else if (item.category === 'architecture') {
                this.inquiryPanelPrompt = '确定放置' + item.name + '吗？';
            } else {
                return;
            }
            this.showInquiryPanel = true;
        },
    },
};
</script>

<style scoped></style>
