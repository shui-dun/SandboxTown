<template>
    <div class="grey-bg">
        <div class="popup-panel">
            <BasicIntroduction v-if="item" :name="item.itemTypeObj.name" :description="item.itemTypeObj.description"
                :image="item.image" />
            <h4 v-if="basicInfo.length > 0">基本信息</h4>
            <ListPanel v-if="basicInfo.length > 0" :items="basicInfo" />
            <h4 v-if="useInfo.length > 0">使用效果</h4>
            <ListPanel v-if="useInfo.length > 0" title="使用效果" :items="useInfo" />
            <h4 v-if="equipInfo.length > 0">装备效果</h4>
            <ListPanel v-if="equipInfo.length > 0" title="装备效果" :items="equipInfo" />
            <h4 v-if="handheldInfo.length > 0">手持效果</h4>
            <ListPanel v-if="handheldInfo.length > 0" title="手持效果" :items="handheldInfo" />
            <div class="button-group">
                <button class="cancel-btn" @click="cancel()">取消</button>
                <button v-if="canBackpack" class="ok-btn" @click="moveToBackpack">放入背包</button>
                <button v-if="canItembar" class="ok-btn" @click="moveToItembar">放入物品栏</button>
                <button v-if="canHandheld" class="ok-btn" @click="hold">手持</button>
                <button v-if="canEquip" class="ok-btn" @click="equip">装备</button>
                <button v-if="canUse" class="ok-btn" @click="use">使用</button>
            </div>
        </div>
    </div>
</template>
  
<script>
import mixin from '@/js/mixin';
import emitter from '@/js/mitt';
import BasicIntroduction from './BasicIntroduction.vue';
import ListPanel from './ListPanel.vue';


export default {
    components: {
        ListPanel,
        BasicIntroduction,
    },
    props: {
        itemId: {
            type: String,
            required: true,
        },
    },
    data() {
        return {
            item: null,
            // 是否可以放入背包
            canBackpack: false,
            // 是否可以放入物品栏
            canItembar: false,
            // 是否可以手持
            canHandheld: false,
            // 是否可以装备
            canEquip: false,
            // 是否可以使用
            canUse: false,
            basicInfo: [],
            useInfo: [],
            equipInfo: [],
            handheldInfo: [],
        };
    },
    methods: {
        async moveToBackpack() {
            // 向后端发送请求，设置背包物品
            await mixin.myPOST(
                '/rest/item/putInBackpack',
                new URLSearchParams({
                    itemId: this.itemId,
                }),
                () => {
                    this.$emit('onConfirm', 'BACKPACK');
                }
            );
        },
        async moveToItembar() {
            // 向后端发送请求，设置物品栏物品
            mixin.myPOST(
                '/rest/item/putInItemBar',
                new URLSearchParams({
                    itemId: this.itemId,
                }),
                () => {
                    this.$emit('onConfirm', 'ITEMBAR');
                }
            );
        },
        async hold() {
            // 向后端发送请求，设置手持物品
            mixin.myPOST(
                '/rest/item/hold',
                new URLSearchParams({
                    itemId: this.itemId,
                }),
                () => {
                    this.$emit('onConfirm', 'HAND');
                }
            );
        },
        // 装备
        async equip() {
            // 向后端发送请求，设置装备
            await mixin.myPOST(
                "/rest/item/equip",
                new URLSearchParams({ itemId: this.itemId }),
                () => {
                    this.$emit('onConfirm', 'EQUIP');
                }
            );
        },
        async use() {
            // 使用
            await mixin.myPOST("/rest/item/use", new URLSearchParams({ itemId: this.itemId }));
            this.$emit('onConfirm', 'USE');
        },
        cancel() {
            this.$emit('onCancel');
        }
    },
    async created() { // created比mounted先执行
        this.item = await mixin.myGET("/rest/item/itemDetail", new URLSearchParams({ itemId: this.itemId }));
        // 设置图片
        this.item.image = require(`@/assets/img/${this.item.itemType}.png`);
        let equipList = ['HELMET', 'CHEST', 'LEG', 'BOOTS'];
        // 判断位置是否在背包
        if (this.item.position == 'BACKPACK') {
            this.canBackpack = false;
            this.canItembar = true;
            this.canHandheld = true;
            // 如果在装备区
        } else if (equipList.includes(this.item.position)) {
            this.canBackpack = true;
            this.canItembar = true;
            this.canHandheld = true;
        } else { // 如果在物品栏或者手持
            this.canBackpack = true;
            this.canItembar = false;
            this.canHandheld = false;
        }

        // 判断是否可以装备
        // 如果物品的位置不在装备区，并且物品是装备类型，可以装备
        this.canEquip = !equipList.includes(this.item.position) && this.item.itemTypeObj.labels.some(label => equipList.includes(label));
        this.canUse = this.item.itemTypeObj.labels.includes('FOOD') || this.item.itemTypeObj.labels.includes('USABLE');
        this.basicInfo = [
            { key: '🔢 数目', value: this.item.itemCount },
            { key: '🚀 等级', value: this.item.level },
        ]
        // 如果耐久度不为-1，说明有寿命，需要显示耐久度以及寿命
        if (this.item.itemTypeObj.durability != -1) {
            this.basicInfo.push({ key: '🔨 耐久', value: this.item.itemTypeObj.durability });
            this.basicInfo.push({ key: '⏳ 寿命', value: this.item.life });
        }
        let attributes = {
            'USE': this.useInfo,
            'EQUIP': this.equipInfo,
            'HANDHELD': this.handheldInfo,
        };
        // 显示＋号
        function showPlusSign(inc) {
            return inc > 0 ? `+${inc}` : `${inc}`;
        }
        for (let operation in this.item.itemTypeObj.attributes) {
            let attribute = this.item.itemTypeObj.attributes[operation];
            let attributeMap = {
                'moneyInc': '金钱',
                'expInc': '经验',
                'levelInc': '等级',
                'hungerInc': '饱腹',
                'hpInc': '血量',
                'attackInc': '攻击',
                'defenseInc': '防御',
                'speedInc': '速度',
                'visionRangeInc': '视野范围',
                'attackRangeInc': '攻击范围',
            }
            for (let key in attributeMap) {
                if (attribute[key] != 0) {
                    attributes[operation].push({ key: `❄ ${attributeMap[key]}`, value: showPlusSign(attribute[key]) });
                }
            }
        }
        for (let operation in this.item.itemTypeObj.effects) {
            let effects = this.item.itemTypeObj.effects[operation];
            // 对于每个效果，都要显示效果的名称和持续时间
            for (let effectId in effects) {
                let effect = effects[effectId];
                let key = `🧪 ${effect.effectObj.name}`;
                let value = `${effect.effectObj.description}`;
                // 如果有持续时间，显示持续时间
                if (effect.duration != -1) {
                    value += `，持续${effect.duration}秒`;
                }
                attributes[operation].push({ key: key, value: value });
            }
        }
    },
};
</script>
  
<style scoped>
.grey-bg {
    position: fixed;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 200;
    background-color: rgba(0, 0, 0, 0.5);
}

.popup-panel {
    display: flex;
    flex-direction: column;
    background-color: #f0f0f0;
    border-radius: 10px;
    padding: 20px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    max-width: 600px;
}

.button-group {
    display: flex;
    width: 100%;
}

.button-group button {
    margin-top: 5px;
    padding-left: 15px;
    padding-right: 15px;
    padding-top: 7px;
    padding-bottom: 7px;
    font-size: 14px;
    color: #fff;
    border: none;
    cursor: pointer;
    border-radius: 5px;
}

.button-group button:not(:last-child) {
    margin-right: 20px;
}

.cancel-btn {
    background-color: #6c757d;
}

.ok-btn {
    background-color: #1165d5;
}
</style>
  