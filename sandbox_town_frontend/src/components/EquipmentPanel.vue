<template>
    <h4 style="margin-bottom: 20px;">⚔️ 装备栏</h4>
    <div class="equipment-tab">
        <table class="custom-table">
            <tbody>
                <tr v-for="item in userInfo" :key='item.label' :id="'tr-' + item.label">
                    <td>{{ item.show }}</td>
                    <td>{{ item.value }}</td>
                </tr>
            </tbody>
        </table>
        <div class="equipment-grid">
            <div class="container">
                <div class="row" style="width: 300px;">
                    <div class="col-6 item" v-for="(item, itemKey) in equipmentItems" :key="itemKey"
                        @click="clickGridItem(itemKey, item)">
                        <div v-if="item.name">
                            <img :src="item.image" :alt="item.name" class="item-image" ref="" />
                            <div class="tool-tip">
                                <h5>{{ item.name }}</h5>
                                <p>{{ item.description }}</p>
                            </div>
                        </div>
                        <div v-else>
                            <img :src='require("@/assets/img/placeholder.jpg")' class="item-image" ref="" />
                        </div>
                        <div class="extra"> {{ itemKey }}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <InquiryPanel v-if="showInquiryPanel" :prompt="inquiryPanelPrompt" @onConfirm="confirm" @onCancel="cancel" />
</template>
<script>
import myUtils from '@/js/myUtils.js';
import InquiryPanel from './InquiryPanel.vue';

export default {
    components: {
        InquiryPanel,
    },
    props: {
    },
    data() {
        return {
            userInfo: [
                { 'label': 'id', 'show': '👨‍💼 名称' },
                { 'label': 'money', 'show': '💰 金钱' },
                { 'label': 'level', 'show': '⬆️ 等级' },
                { 'label': 'exp', 'show': '🍾 经验值' },
                { 'label': 'hunger', 'show': '🥪 饱腹值' },
                { 'label': 'attack', 'show': '⚔️ 攻击力' },
                { 'label': 'defense', 'show': '🛡️ 防御力' },
                { 'label': 'speed', 'show': '🏃 速度' },
                { 'label': 'hp', 'show': '🩸 血量' },
            ],
            equipmentItems: {
                '护甲': {},
                '鞋子': {},
                '左手': {},
                '右手': { id: 2, name: '锯子', image: require("@/assets/img/saw.png"), category: 'equipment', description: '简单而有效的切割工具' },
            },
            // 选择的物品
            selectedItem: null,
            selectedItemKey: null,
            showInquiryPanel: false,
            inquiryPanelPrompt: '',
        };
    },
    methods: {
        clickGridItem(itemKey, item) {
            if (item.name) {
                this.inquiryPanelPrompt = '确定卸下' + item.name + '吗？';
                this.selectedItem = item;
                this.selectedItemKey = itemKey;
                this.showInquiryPanel = true;
            }
        },
        cancel() {
            this.showInquiryPanel = false;
        },
        confirm() {
            this.equipmentItems[this.selectedItemKey] = {};
            myUtils.fadeInfoShow(`卸下${this.selectedItem.name}`)
            this.showInquiryPanel = false;
        },
    },
    mounted() {
        // 从后端获取玩家信息
        myUtils.myGET('/rest/sprite/listMine', null, (data) => {
            data.id = data.id.split("_", 2)[1];
            this.player = data;
            // 将用户信息添加到userInfo中
            this.userInfo.forEach((item) => {
                item.value = this.player[item.label];
            });
        });
    },
};
</script>
<style scoped>
.equipment-tab {
    display: flex;
}

.custom-table th,
.custom-table td {
    padding-bottom: 10px;
    padding-right: 30px;
    text-align: left;
}


.item {
    text-align: center;
    margin-bottom: 1rem;
    cursor: pointer;
    position: relative;
}

.item-image {
    width: 100%;
    height: 100%;
    border-radius: 5px;
}

.tool-tip {
    display: none;
    z-index: 101;
}

.item:hover .tool-tip {
    display: block;
    position: absolute;
    background-color: #f9f9f9;
    border: 1px solid #ccc;
    border-radius: 4px;
    padding: 8px;
    font-size: 14px;
    top: 40px;
    left: 40px;
    width: 100px;
}

.caption {
    background-color: #ddd;
    border-radius: 5px;
    margin-top: 5px;
    margin-bottom: 3px;
    font-size: 14px;
}

.equipment-grid {
    margin-left: 20px;
}
</style>