<template>
    <div>
        <NavGroup :items="componentItems" :initTab="initTab" @close="$emit('close')"></NavGroup>
    </div>
</template>

<script>
import NavGroup from './NavGroup.vue';

export default {
    components: {
        NavGroup
    },
    data() {
        return {
            // 用户可以买的物品
            buyItems: [
                { id: 1, name: '面包', image: require("@/assets/img/bread.png"), category: 'food', description: '具有松软的质地和微甜的口感', extra: { price: 10 } },
                { id: 2, name: '锯子', image: require("@/assets/img/saw.png"), category: 'item', description: '简单而有效的切割工具', extra: { price: 12 } },
                { id: 3, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火', extra: { price: 8 } },
                { id: 4, name: '猫咪', image: require("@/assets/img/cat.png"), category: 'pet', description: '常见的家养宠物，具有柔软的毛发和灵活的身体', extra: { price: 20 } },
                { id: 5, name: '柯基', image: require("@/assets/img/dog.png"), category: 'pet', description: '可靠的护卫，忠诚而勇敢，像你的影子一样一直陪伴着你', extra: { price: 20 } },
                { id: 6, name: '苹果', image: require("@/assets/img/apple.png"), category: 'food', description: '禁忌和知识的诱惑', extra: { price: 13 } },
                { id: 7, name: '面包', image: require("@/assets/img/bread.png"), category: 'food', description: '具有松软的质地和微甜的口感', extra: { price: 10 } },
                { id: 8, name: '锯子', image: require("@/assets/img/saw.png"), category: 'item', description: '简单而有效的切割工具', extra: { price: 12 } },
                { id: 9, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火', extra: { price: 8 } },
                { id: 10, name: '猫咪', image: require("@/assets/img/cat.png"), category: 'pet', description: '常见的家养宠物，具有柔软的毛发和灵活的身体', extra: { price: 20 } },
                { id: 11, name: '柯基', image: require("@/assets/img/dog.png"), category: 'pet', description: '可靠的护卫，忠诚而勇敢，像你的影子一样一直陪伴着你', extra: { price: 20 } },
                { id: 12, name: '苹果', image: require("@/assets/img/apple.png"), category: 'food', description: '禁忌和知识的诱惑', extra: { price: 13 } }
            ],
            // 用户可以卖的物品
            soldItems: [
                { id: 1, name: '面包', image: require("@/assets/img/bread.png"), category: 'food', description: '具有松软的质地和微甜的口感', extra: { price: 10 } },
                { id: 2, name: '锯子', image: require("@/assets/img/saw.png"), category: 'item', description: '简单而有效的切割工具', extra: { price: 12 } },
                { id: 3, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火', extra: { price: 8 } },
                { id: 4, name: '猫咪', image: require("@/assets/img/cat.png"), category: 'pet', description: '常见的家养宠物，具有柔软的毛发和灵活的身体', extra: { price: 20 } },
                { id: 5, name: '柯基', image: require("@/assets/img/dog.png"), category: 'pet', description: '可靠的护卫，忠诚而勇敢，像你的影子一样一直陪伴着你', extra: { price: 20 } },
                { id: 6, name: '苹果', image: require("@/assets/img/apple.png"), category: 'food', description: '禁忌和知识的诱惑', extra: { price: 13 } },
                { id: 7, name: '面包', image: require("@/assets/img/bread.png"), category: 'food', description: '具有松软的质地和微甜的口感', extra: { price: 10 } },
                { id: 8, name: '锯子', image: require("@/assets/img/saw.png"), category: 'item', description: '简单而有效的切割工具', extra: { price: 12 } },
                { id: 9, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火', extra: { price: 8 } },
                { id: 10, name: '猫咪', image: require("@/assets/img/cat.png"), category: 'pet', description: '常见的家养宠物，具有柔软的毛发和灵活的身体', extra: { price: 20 } },
                { id: 11, name: '柯基', image: require("@/assets/img/dog.png"), category: 'pet', description: '可靠的护卫，忠诚而勇敢，像你的影子一样一直陪伴着你', extra: { price: 20 } },
                { id: 12, name: '苹果', image: require("@/assets/img/apple.png"), category: 'food', description: '禁忌和知识的诱惑', extra: { price: 13 } }
            ],
            initTab: 'buyTab',
            categories: [
                { 'label': 'food', 'prompt': '食物🥪' },
                { 'label': 'item', 'prompt': '物品🔨' },
                { 'label': 'pet', 'prompt': '宠物🐶' }
            ],
            componentItems: []
        };
    },
    mounted() {
        this.componentItems = [
            {
                label: '买入',
                tabName: 'buyTab',
                name: 'GridItems',
                props: { title: '🏪 购买商品', items: this.buyItems, categories: this.categories },
            },
            {
                label: '卖出',
                tabName: 'soldTab',
                name: 'GridItems',
                props: { title: '🏬 卖出商品', items: this.soldItems, categories: this.categories },
            }
        ]
    },
    computed: {
    },
    methods: {
    },
};
</script>

<style scoped></style>
