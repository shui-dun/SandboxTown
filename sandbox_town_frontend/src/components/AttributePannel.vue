<template>
    <div>
        <NavGroup @close="$emit('close')">
            <template v-slot:0>
                <InfoPannel title="🔍 基础信息" :items="itemInfo" />
            </template>
        </NavGroup>
    </div>
</template>

<script>
import NavGroup from './NavGroup.vue';
import InfoPannel from './ListPannel.vue';
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
        InfoPannel,
    },
    data() {
        return {
            info: {
            },
            itemInfo: [
                { 'label': 'id', 'show': '🆔 ID' },
                { 'label': 'description', 'show': '📝 介绍' },
                { 'label': 'owner', 'show': '👤 拥有者' },
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
        this.info = await myUtils.myGET(`/rest/sprite/list/${this.itemName}`);
        // 如果是用户，删掉前缀
        if (this.itemName.startsWith("user_")) {
            this.info.id = this.info.id.split("_", 2)[1];
        }
        // 将信息添加到userInfo中
        this.itemInfo.forEach((item) => {
            if (this.info[item.label] !== null) {
                item.value = this.info[item.label];
            }
        });
    },
    computed: {
    },
    methods: {
    },
    inject: ['fadeInfoShow'],
};
</script>

<style scoped></style>
