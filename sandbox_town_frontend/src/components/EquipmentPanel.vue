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
                            <img :src='require("@/assets/img/PLACEHOLDER.jpg")' class="item-image" ref="" />
                        </div>
                        <div class="caption"> {{ itemKey }}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <ItemDetail v-if="showItemDetail" :itemId="selectedItem.id" @onConfirm="confirm" @onCancel="cancel" />
</template>
<script>
import mixin from '@/js/mixin.js';
import ItemDetail from './ItemDetail.vue';

export default {
    components: {
        ItemDetail,
    },
    props: {
    },
    data() {
        return {
            userInfo: [],
            equipmentItems: {},
            // 选择的物品
            selectedItem: null,
            showItemDetail: false,
        };
    },
    methods: {
        clickGridItem(itemKey, item) {
            if (item.name) {
                this.selectedItem = item;
                this.showItemDetail = true;
            }
        },
        cancel() {
            this.showItemDetail = false;
        },
        confirm() {
            this.showItemDetail = false;
            this.refreshUserInfo();
            this.refreshEquipment();
        },
        refreshUserInfo() {
            this.userInfo = [
                { 'label': 'id', 'show': '👨‍💼 名称' },
                { 'label': 'money', 'show': '💰 金钱' },
                { 'label': 'level', 'show': '⬆️ 等级' },
                { 'label': 'exp', 'show': '🍾 经验值' },
                { 'label': 'hunger', 'show': '🥪 饱腹值' },
                { 'label': 'attack', 'show': '⚔️ 攻击力' },
                { 'label': 'defense', 'show': '🛡️ 防御力' },
                { 'label': 'speed', 'show': '🏃 速度' },
                { 'label': 'hp', 'show': '🩸 血量' },
                { 'label': 'visionRange', 'show': '👀 视野范围' },
                { 'label': 'attackRange', 'show': '🎯 攻击范围' },
            ];
            // 从后端获取玩家信息
            mixin.myGET('/rest/sprite/listMine', null, (data) => {
                data.id = data.id.split("_", 2)[1];
                this.player = data;
                // 将用户信息添加到userInfo中
                this.userInfo.forEach((item) => {
                    // 显示基础属性值
                    item.value = this.player[item.label];
                    // 如果有增量属性值，则显示增量属性值
                    if (this.player[item.label + 'Inc']) {
                        item.value += '+' + this.player[item.label + 'Inc'];
                    }
                });
            });
        },
        refreshEquipment() {
            this.equipmentItems = {
                '头盔': {},
                '胸甲': {},
                '护腿': {},
                '鞋子': {},
            };
            // 从后端获取玩家装备信息
            mixin.myGET('/rest/item/listMyItemsInEquipment', null, (data) => {
                // 将用户装备信息添加到equipmentItems中
                for (let i = 0; i < data.length; i++) {
                    let item = data[i];
                    // item.id = item.id;
                    item.name = item.itemTypeObj.name;
                    item.image = require("@/assets/img/" + item.itemType + ".png");
                    item.description = item.itemTypeObj.description;
                    if (item.position == 'HELMET') {
                        this.equipmentItems['头盔'] = item;
                    } else if (item.position == 'CHEST') {
                        this.equipmentItems['胸甲'] = item;
                    } else if (item.position == 'LEG') {
                        this.equipmentItems['护腿'] = item;
                    } else if (item.position == 'BOOTS') {
                        this.equipmentItems['鞋子'] = item;
                    }
                }
            });
        },
    },
    mounted() {
        this.refreshUserInfo();
        this.refreshEquipment();
    },
};
</script>
<style scoped>
.equipment-tab {
    display: flex;
}

.custom-table th,
.custom-table td {
    padding-bottom: 0px;
    padding-right: 30px;
    text-align: left;
}


.item {
    text-align: center;
    margin-bottom: 5px;
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
    margin-bottom: 30px;
    font-size: 14px;
}

.equipment-grid {
    margin-left: 20px;
}
</style>