<template>
    <div>
        <NavGroup :items="componentItems" @close="$emit('close')">
            <template v-slot:0>
                <GridPanel ref="bugGridPanel" title="🏪 购买商品" :items="this.buyItems" :categories="this.categories"
                    @clickGridItem="bugItemEvent($event)" />
            </template>
            <template v-slot:1>
                <GridPanel title="🏬 卖出商品" :items="this.soldItems" :categories="this.categories"
                    @clickGridItem="soldItemEvent($event)" />
            </template>
        </NavGroup>
        <NumberChoose v-if="showNumberChoose" :maxNumber="maxNumber" @onConfirm="confirm" @onCancel="cancel" />
    </div>
</template>

<script>
import NavGroup from './NavGroup.vue';
import GridPanel from './GridPanel.vue';
import NumberChoose from './NumberChoose.vue';
import myUtils from "@/js/myUtils.js";

export default {
    props: {
        storeId: {
            type: String,
            default: '',
        },
    },
    components: {
        NavGroup,
        GridPanel,
        NumberChoose,
    },
    data() {
        return {
            showNumberChoose: false,
            // 用户可以买的物品
            // 对于食物和物品，ID就是类别，例如bread，对于宠物和装备，ID就是ID
            buyItems: [
            ],
            // 用户可以卖的物品
            soldItems: [
                { id: 1, name: '面包', image: require("@/assets/img/BREAD.png"), category: 'item', description: '具有松软的质地和微甜的口感', extra: { price: '￥10', num: 1 } },
            ],
            categories: [
                { 'label': 'ITEM', 'prompt': '物品' },
                { 'label': 'EQUIPMENT', 'prompt': '装备' },
                { 'label': 'PET', 'prompt': '宠物' },
                { 'label': 'ARCHITECTURE', 'prompt': '建筑' },
            ],
            componentItems: ['买入', '卖出'],
            // 想要买入还是卖出
            willingOperation: '',
            // 选择的物品
            selectedItem: {},
            // 想要买入或卖出的数目
            willingNumber: 0,
            // 可以买入或卖出的最大数目
            maxNumber: 0,
        };
    },
    async mounted() {
        // 获得商品列表
        await myUtils.myGET('/rest/store/listByStore',
            new URLSearchParams({
                store: this.storeId,
            }),
        ).then((goods) => {
            goods.forEach((item) => {
                item.id = item.item;
                item.image = require(`@/assets/img/${item.id}.png`);
                item.category = 'ITEM';
                item.caption = { price: '￥' + item.price, num: item.count };
                this.buyItems.push(item);
            });
        });
        this.$refs.bugGridPanel.filterItems('ALL');
    },
    computed: {
    },
    methods: {
        bugItemEvent(item) {
            this.willingOperation = 'BUY';
            this.selectedItem = item;
            this.maxNumber = item.caption.num;
            this.showNumberChoose = true;
        },
        soldItemEvent(item) {
            if (item.caption.num === 0) {
                myUtils.fadeInfoShow(`你没有${item.name}了`)
                return;
            }
            this.willingOperation = 'SOLD';
            this.selectedItem = item;
            this.maxNumber = item.caption.num;
            this.showNumberChoose = true;
        },
        async confirm(value) {
            this.willingNumber = value;
            if (this.willingOperation === 'BUY') {
                // 处理购买请求
                await myUtils.myPOST('/rest/store/buy',
                    new URLSearchParams({
                        store: this.storeId,
                        item: this.selectedItem.id,
                        amount: this.willingNumber,
                    }),
                    () => {
                        // 由父节点显示提示信息
                        myUtils.fadeInfoShow(`购买${this.willingNumber}个${this.selectedItem.name}`)
                        // 更新商品列表中该商品的数目
                        this.selectedItem.caption.num -= this.willingNumber;
                    },
                )
            } else if (this.willingOperation === 'SOLD') {
                // 处理出售请求
                let item = this.selectedItem;
                item.caption.num -= this.willingNumber;
                // 由父节点显示提示信息
                myUtils.fadeInfoShow(`出售${this.willingNumber}个${this.selectedItem.name}`)
            }
            this.showNumberChoose = false;
        },
        cancel() {
            this.showNumberChoose = false;
        },
    },
};
</script>

<style scoped></style>
