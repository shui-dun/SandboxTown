<template>
    <div>
        <NavGroup @close="$emit('close')">
            <template v-slot:0>
                <InfoList title="🔍 基础信息" :items="itemInfo" />
            </template>
        </NavGroup>
    </div>
</template>

<script>
import NavGroup from './NavGroup.vue';
import InfoList from './InfoList.vue';

export default {
    components: {
        NavGroup,
        InfoList,
    },
    data() {
        return {
            info: {
                username: 'Player2',
                money: 1000,
                level: 5,
                exp: 200,
                hunger: 50,
                attack: 10,
                defense: 10,
                speed: 8,
                hp: 100,
            },
            itemInfo: [
                { 'label': 'username', 'show': '👨‍💼 用户名' },
                { 'label': 'money', 'show': '💰 金钱' },
                { 'label': 'level', 'show': '⬆️ 等级' },
                { 'label': 'exp', 'show': '🍾 经验值' },
                { 'label': 'hunger', 'show': '🥪 饱腹值' },
                { 'label': 'attack', 'show': '⚔️ 攻击力' },
                { 'label': 'defense', 'show': '🛡️ 防御力' },
                { 'label': 'speed', 'show': '🏃 速度' },
                { 'label': 'hp', 'show': '🩸 血量' },
            ],
            componentItems: []
        };
    },
    mounted() {
        // 将信息添加到userInfo中
        this.itemInfo.forEach((item) => {
            item.value = this.info[item.label];
        });
    },
    computed: {
    },
    methods: {
    },
};
</script>

<style scoped></style>
