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
    props: {
        itemName: {
            type: String,
            required: true,
        },
    },
    components: {
        NavGroup,
        InfoList,
    },
    data() {
        return {
            info: {
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
    async mounted() {
        // 从后端获取物品信息
        if (this.itemName.startsWith("user_")) {
            await fetch(`/rest/player/list/${this.itemName}`, {
                method: 'GET',
            }).then(response => response.json())
                .then(data => {
                    if (data.code === 0) {
                        this.info = data.data;
                        this.info.username = this.info.username.split("_", 2)[1];
                    } else {
                        this.fadeInfoShow(data.msg);
                    }
                });
        }
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
