<template>
    <!-- 组件的左侧是一个导航栏，包含2个栏目：基础信息、物品栏 -->
    <!-- 组件的右侧显示详细内容-->
    <!-- 当点击导航栏的基础信息时，详细内容区域显示玩家的基础信息，包括用户名、金钱数目、经验值、等级、饥饿值、血量 -->
    <!-- 当点击导航栏的物品栏时，详细内容区域显示玩家拥有的物品 -->
    <!-- 物品展示区域从上往下包含3部分，上部分是物品类别选择区域，包含全部、食品、装备、物品这几个类别 -->
    <!-- 中间部分是物品网格，请使用网格布局展示玩家的物品，每一格物品包含两部分，上侧展示物品的图片，下侧展示物品的名称，并且当光标悬浮在物品上时，显示物品的详细介绍。点击对应的类别将显示对应类别的物品。 -->
    <!-- 下部分是翻页栏，包含上一页和下一页两个按钮，点击即可实现翻页功能 -->
    <!-- 这个组件我想要始终位于页面的正中央，并且是在页面的最上层，不管下层的页面元素如何变化，这个组件都不受影响 -->
    <!-- 最后，请在组下右上角增添一个关闭按钮 -->
    <div>
        <NavGroup :items="tabs" @close="$emit('close')">
            <template v-slot:0>
                <InfoList title="🔍 基础信息" :items="this.userInfo" />
            </template>
            <template v-slot:1>
                <GridItems title="🎒 物品栏" :items="this.player.items" :categories="this.categories" />
            </template>
        </NavGroup>
    </div>
</template>

<script>
import NavGroup from './NavGroup.vue';
import InfoList from './InfoList.vue';
import GridItems from './GridItems.vue';

export default {
    components: {
        NavGroup,
        InfoList,
        GridItems
    },
    data() {
        return {
            player: {
                username: 'Player1',
                money: 1000,
                exp: 200,
                level: 5,
                hunger: 50,
                hp: 100,
                items: [
                    { id: 1, name: '面包', image: require("@/assets/img/bread.png"), category: 'food', description: '具有松软的质地和微甜的口感', extra: { num: 1 } },
                    { id: 2, name: '锯子', image: require("@/assets/img/saw.png"), category: 'equipment', description: '简单而有效的切割工具', extra: { num: 1 } },
                    { id: 3, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火', extra: { num: 1 } },
                    { id: 4, name: '猫咪', image: require("@/assets/img/cat.png"), category: 'pet', description: '常见的家养宠物，具有柔软的毛发和灵活的身体', extra: { num: 1 } },
                    { id: 5, name: '柯基', image: require("@/assets/img/dog.png"), category: 'pet', description: '可靠的护卫，忠诚而勇敢，像你的影子一样一直陪伴着你', extra: { num: 1 } },
                    { id: 6, name: '苹果', image: require("@/assets/img/apple.png"), category: 'food', description: '禁忌和知识的诱惑', extra: { num: 1 } },
                    { id: 7, name: '面包', image: require("@/assets/img/bread.png"), category: 'food', description: '具有松软的质地和微甜的口感', extra: { num: 1 } },
                    { id: 8, name: '锯子', image: require("@/assets/img/saw.png"), category: 'equipment', description: '简单而有效的切割工具', extra: { num: 1 } },
                    { id: 9, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火', extra: { num: 1 } },
                    { id: 10, name: '猫咪', image: require("@/assets/img/cat.png"), category: 'pet', description: '常见的家养宠物，具有柔软的毛发和灵活的身体', extra: { num: 1 } },
                    { id: 11, name: '柯基', image: require("@/assets/img/dog.png"), category: 'pet', description: '可靠的护卫，忠诚而勇敢，像你的影子一样一直陪伴着你', extra: { num: 1 } },
                    { id: 12, name: '苹果', image: require("@/assets/img/apple.png"), category: 'food', description: '禁忌和知识的诱惑', extra: { num: 1 } }
                ],
            },
            userInfo: [
                { 'label': 'username', 'show': '👨‍💼 用户名' },
                { 'label': 'money', 'show': '💰 金钱数目' },
                { 'label': 'exp', 'show': '🍾 经验值' },
                { 'label': 'level', 'show': '⬆️ 等级' },
                { 'label': 'hunger', 'show': '🥪 饱腹值' },
                { 'label': 'hp', 'show': '🩸 血量' }
            ],
            categories: [
                { 'label': 'food', 'prompt': '食物' },
                { 'label': 'item', 'prompt': '物品' },
                { 'label': 'equipment', 'prompt': '装备' },
                { 'label': 'pet', 'prompt': '宠物' },
            ],
            tabs: []
        };
    },
    mounted() {
        // 将player中的信息添加到userInfo中
        this.userInfo.forEach((item) => {
            item.value = this.player[item.label];
        });
        this.tabs = ['基础信息', '物品栏'];
    },
    computed: {
    },
    methods: {

    },
};
</script>

<style scoped></style>
