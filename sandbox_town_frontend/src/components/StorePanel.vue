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
                // { id: 1, name: '面包', image: require("@/assets/img/bread.png"), category: 'item', description: '具有松软的质地和微甜的口感', extra: { price: '￥10' } },
                // { id: 2, name: '锯子', image: require("@/assets/img/saw.png"), category: 'equipment', description: '简单而有效的切割工具', extra: { price: '￥12' } },
                // { id: 3, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火', extra: { price: '￥8' } },
                // { id: 4, name: '猫咪', image: require("@/assets/img/cat.png"), category: 'pet', description: '常见的家养宠物，具有柔软的毛发和灵活的身体', extra: { price: '￥20' } },
                // { id: 5, name: '柴犬', image: require("@/assets/img/dog.png"), category: 'pet', description: '可靠的护卫，忠诚而勇敢，像你的影子一样一直陪伴着你', extra: { price: '￥20' } },
                // { id: 6, name: '苹果', image: require("@/assets/img/apple.png"), category: 'item', description: '禁忌和知识之果', extra: { price: '￥13' } },
                // { id: 7, name: '面包', image: require("@/assets/img/bread.png"), category: 'item', description: '具有松软的质地和微甜的口感', extra: { price: '￥10' } },
                // { id: 8, name: '锯子', image: require("@/assets/img/saw.png"), category: 'equipment', description: '简单而有效的切割工具', extra: { price: '￥12' } },
                // { id: 9, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火', extra: { price: '￥8' } },
                // { id: 10, name: '猫咪', image: require("@/assets/img/cat.png"), category: 'pet', description: '常见的家养宠物，具有柔软的毛发和灵活的身体', extra: { price: '￥20' } },
                // { id: 11, name: '柴犬', image: require("@/assets/img/dog.png"), category: 'pet', description: '可靠的护卫，忠诚而勇敢，像你的影子一样一直陪伴着你', extra: { price: '￥20' } },
                // { id: 12, name: '苹果', image: require("@/assets/img/apple.png"), category: 'item', description: '禁忌和知识之果', extra: { price: '￥13' } },
                // { id: 13, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火', extra: { price: '￥8' } },
                // { id: 14, name: '猫咪', image: require("@/assets/img/cat.png"), category: 'pet', description: '常见的家养宠物，具有柔软的毛发和灵活的身体', extra: { price: '￥20' } },
                // { id: 15, name: '柴犬', image: require("@/assets/img/dog.png"), category: 'pet', description: '可靠的护卫，忠诚而勇敢，像你的影子一样一直陪伴着你', extra: { price: '￥20' } },
                // { id: 16, name: '苹果', image: require("@/assets/img/apple.png"), category: 'item', description: '禁忌和知识之果', extra: { price: '￥13' } },
            ],
            // 用户可以卖的物品
            soldItems: [
                { id: 1, name: '面包', image: require("@/assets/img/bread.png"), category: 'item', description: '具有松软的质地和微甜的口感', extra: { price: '￥10', num: 1 } },
                { id: 2, name: '锯子', image: require("@/assets/img/saw.png"), category: 'equipment', description: '简单而有效的切割工具', extra: { price: '￥12', num: 3 } },
                { id: 3, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火', extra: { price: '￥8', num: 1 } },
                { id: 4, name: '猫咪', image: require("@/assets/img/cat.png"), category: 'pet', description: '常见的家养宠物，具有柔软的毛发和灵活的身体', extra: { price: '￥20', num: 1 } },
                { id: 5, name: '柴犬', image: require("@/assets/img/dog.png"), category: 'pet', description: '可靠的护卫，忠诚而勇敢，像你的影子一样一直陪伴着你', extra: { price: '￥20', num: 1 } },
                { id: 6, name: '苹果', image: require("@/assets/img/apple.png"), category: 'item', description: '禁忌和知识之果', extra: { price: '￥13', num: 1 } },
                { id: 7, name: '面包', image: require("@/assets/img/bread.png"), category: 'item', description: '具有松软的质地和微甜的口感', extra: { price: '￥10', num: 1 } },
                { id: 8, name: '锯子', image: require("@/assets/img/saw.png"), category: 'equipment', description: '简单而有效的切割工具', extra: { price: '￥12', num: 1 } },
                { id: 9, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火', extra: { price: '￥8', num: 1 } },
                { id: 10, name: '猫咪', image: require("@/assets/img/cat.png"), category: 'pet', description: '常见的家养宠物，具有柔软的毛发和灵活的身体', extra: { price: '￥20', num: 1 } },
                { id: 11, name: '柴犬', image: require("@/assets/img/dog.png"), category: 'pet', description: '可靠的护卫，忠诚而勇敢，像你的影子一样一直陪伴着你', extra: { price: '￥20', num: 1 } },
                { id: 12, name: '苹果', image: require("@/assets/img/apple.png"), category: 'item', description: '禁忌和知识之果', extra: { price: '￥13', num: 1 } }
            ],
            categories: [
                { 'label': 'item', 'prompt': '物品' },
                { 'label': 'equipment', 'prompt': '装备' },
                { 'label': 'pet', 'prompt': '宠物' },
                { 'label': 'architecture', 'prompt': '建筑' },
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
                item.category = 'item';
                item.caption = { price: '￥' + item.price, num: item.count };
                this.buyItems.push(item);
            });
        });
        this.$refs.bugGridPanel.filterItems('all');
    },
    computed: {
    },
    methods: {
        bugItemEvent(item) {
            this.willingOperation = 'buy';
            this.selectedItem = item;
            this.maxNumber = item.caption.num;
            this.showNumberChoose = true;
        },
        soldItemEvent(item) {
            if (item.caption.num === 0) {
                myUtils.fadeInfoShow(`你没有${item.name}了`)
                return;
            }
            this.willingOperation = 'sold';
            this.selectedItem = item;
            this.maxNumber = item.caption.num;
            this.showNumberChoose = true;
        },
        async confirm(value) {
            this.willingNumber = value;
            if (this.willingOperation === 'buy') {
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
            } else if (this.willingOperation === 'sold') {
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
