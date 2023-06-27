<template>
    <div class="grey-bg">
        <div class="popup-panel">
            <div class="popup-panel-header">
                <!-- 物品名称 -->
                <p>{{ (item == null) ? '' : item.itemTypeObj.name }}</p>
            </div>
            <div class="popup-panel-content">{{ (item == null) ? '' : item.itemTypeObj.description }}</div>
            <h4 v-if="basicInfo.length > 0" >基本信息</h4>
            <ListPanel v-if="basicInfo.length > 0" :items="basicInfo" />
                <h4 v-if="useInfo.length > 0" >使用效果</h4>
            <ListPanel v-if="useInfo.length > 0" title="使用效果" :items="useInfo" />
                <h4 v-if="equipInfo.length > 0" >装备效果</h4>
            <ListPanel v-if="equipInfo.length > 0" title="装备效果" :items="equipInfo" />
                <h4 v-if="handheldInfo.length > 0" >手持效果</h4>
            <ListPanel v-if="handheldInfo.length > 0" title="手持效果" :items="handheldInfo" />
            <div class="button-group">
                <button class="cancel-btn" @click="cancel()">取消</button>
                <button class="ok-btn" @click="confirm('ITEMBAR')">放入物槽</button>
                <button class="ok-btn" @click="confirm('HAND')">手持</button>
                <button v-if="canEquip" class="ok-btn" @click="confirm('EQUIP')">装备</button>
                <button v-if="canUse" class="ok-btn" @click="confirm('USE')">使用</button>
            </div>
        </div>
    </div>
</template>
  
<script>
import myUtils from '@/js/myUtils';
import ListPanel from './ListPanel.vue';


export default {
    components: {
        ListPanel,
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
        confirm(event) {
            this.$emit('onConfirm');
        },
        cancel() {
            this.$emit('onCancel');
        }
    },
    async mounted() {
        this.item = await myUtils.myGET("/rest/item/itemDetail", new URLSearchParams({ itemId: this.itemId }));
        this.canEquip = this.item.itemTypeObj.labels.includes('HELMET') || this.item.itemTypeObj.labels.includes('CHEST')
            || this.item.itemTypeObj.labels.includes('LEG') || this.item.itemTypeObj.labels.includes('BOOTS');
        this.canUse = this.item.itemTypeObj.labels.includes('FOOD') || this.item.itemTypeObj.labels.includes('USABLE');
        this.durability = this.item.itemTypeObj.durability;
        this.basicInfo = [
            { key: '🔢 数目', value: this.item.itemCount },

            { key: '⭐ 等级', value: this.item.level },

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
            if (attribute.moneyInc != 0) {
                attributes[operation].push({ key: '❄ 金钱', value: showPlusSign(attribute.moneyInc) });
            }
            if (attribute.expInc != 0) {
                attributes[operation].push({ key: '❄ 经验', value: showPlusSign(attribute.expInc) });
            }
            if (attribute.levelInc != 0) {
                attributes[operation].push({ key: '❄ 等级', value: showPlusSign(attribute.levelInc) });
            }
            if (attribute.hungerInc != 0) {
                attributes[operation].push({ key: '❄ 饱腹', value: showPlusSign(attribute.hungerInc) });
            }
            if (attribute.hpInc != 0) {
                attributes[operation].push({ key: '❄ 生命', value: showPlusSign(attribute.hpInc) });
            }
            if (attribute.attackInc != 0) {
                attributes[operation].push({ key: '❄ 攻击', value: showPlusSign(attribute.attackInc) });
            }
            if (attribute.defenseInc != 0) {
                attributes[operation].push({ key: '❄ 防御', value: showPlusSign(attribute.defenseInc) });
            }
            if (attribute.speedInc != 0) {
                attributes[operation].push({ key: '❄ 速度', value: showPlusSign(attribute.speedInc) });
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

.popup-panel-header {
    font-size: 28px;
    font-weight: bold;
    color: #333;
}

.popup-panel-content {
    font-size: 18px;
    color: #333;
    margin-bottom: 20px;
}

.button-group {
    display: flex;
    width: 100%;
}

.button-group button {
    margin-top: 20px;
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
  