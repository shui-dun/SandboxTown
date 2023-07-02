<template>
    <div>
        <NavGroup @close="$emit('close')">
            <template v-slot:0>
                <div style="max-width: 250px;">
                    <h4>{{ id }}</h4>
                    <div style="margin-bottom: 15px;">{{ description }}</div>
                    <ListPanel :items="itemInfo" />
                </div>
            </template>
        </NavGroup>
    </div>
</template>

<script>
import NavGroup from './NavGroup.vue';
import ListPanel from './ListPanel.vue';
import mixin from "@/js/mixin.js";

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
            // 名称
            id: '',
            // 介绍
            description: '',
        };
    },
    async mounted() {
        // 从后端获取物品信息
        this.info = await mixin.myGET(`/rest/sprite/list/${this.itemName}`);
        // 如果是用户，删掉前缀
        if (this.itemName.startsWith("USER_")) {
            this.info.id = this.info.id.split("_", 2)[1];
        } else {
            // 否则对名字进行哈希
            this.info.id = mixin.hashName(this.info.id);
        }
        this.id = this.info.id;
        this.description = this.info.description;
        // 如果主人是用户，删掉前缀
        if (this.info.owner != null && this.info.owner.startsWith("USER_")) {
            this.info.owner = this.info.owner.split("_", 2)[1];
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
