<template>
    <div>
        <NavGroup @close="$emit('close')">
            <template v-slot:0>
                <div style="max-width: 400px;">
                    <ListPanel title="🔍 基础信息" :items="itemInfo" />
                </div>
            </template>
        </NavGroup>
    </div>
</template>

<script>
import NavGroup from './NavGroup.vue';
import ListPanel from './ListPanel.vue';
import myUtils from "@/js/myUtils.js";

export default {
    props: {
        itemName: {
            type: String,
            required: true,
        },
    },
    components: {
        NavGroup,
        ListPanel,
    },
    data() {
        return {
            info: {
            },
            itemInfo: [
                { 'id': 'id', 'key': '🆔 ID' },
                { 'id': 'description', 'key': '📝 介绍' },
                { 'id': 'owner', 'key': '👤 拥有者' },
                { 'id': 'money', 'key': '💰 金钱' },
                { 'id': 'level', 'key': '⬆️ 等级' },
                { 'id': 'exp', 'key': '🍾 经验值' },
                { 'id': 'hunger', 'key': '🥪 饱腹值' },
                { 'id': 'attack', 'key': '⚔️ 攻击力' },
                { 'id': 'defense', 'key': '🛡️ 防御力' },
                { 'id': 'speed', 'key': '🏃 速度' },
                { 'id': 'hp', 'key': '🩸 血量' },
            ],
            componentItems: []
        };
    },
    async mounted() {
        // 从后端获取物品信息
        this.info = await myUtils.myGET(`/rest/sprite/list/${this.itemName}`);
        // 如果是用户，删掉前缀
        if (this.itemName.startsWith("USER_")) {
            this.info.id = this.info.id.split("_", 2)[1];
        }
        // 将信息添加到userInfo中
        this.itemInfo.forEach((item) => {
            if (this.info[item.id] !== null) {
                item.value = this.info[item.id];
            }
        });
    },
    computed: {
    },
    methods: {
    },
};
</script>

<style scoped></style>
