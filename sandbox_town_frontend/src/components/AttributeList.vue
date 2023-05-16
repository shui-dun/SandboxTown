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
            initTab: 'infoTab',
            info: {
                username: 'Player2',
                money: 1000,
                exp: 200,
                level: 5,
                hunger: 50,
                hp: 100,
            },
            itemInfo: [
                { 'label': 'username', 'show': '👨‍💼 名称' },
                { 'label': 'money', 'show': '💰 金钱数目' },
                { 'label': 'exp', 'show': '🍾 经验值' },
                { 'label': 'level', 'show': '⬆️ 等级' },
                { 'label': 'hunger', 'show': '🥪 饱腹值' },
                { 'label': 'hp', 'show': '🩸 血量' }
            ],
            componentItems: []
        };
    },
    mounted() {
        // 将信息添加到userInfo中
        this.itemInfo.forEach((item) => {
            item.value = this.info[item.label];
        });
        this.componentItems = [
            {
                label: '',
                tabName: 'infoTab',
                name: 'InfoList',
                props: { title: '🔍 基础信息', data: this.itemInfo },
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
