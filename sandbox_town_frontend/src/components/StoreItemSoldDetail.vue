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
                <div>
                    <button @click="decrement"><svg width="25" height="25" xmlns="http://www.w3.org/2000/svg"
                            viewBox="0 0 24 24">
                            <path d="M8 12L14 6V18L8 12Z"></path>
                        </svg></button>
                    <span>{{ number }}</span>
                    <button @click="increment"><svg width="25" height="25" xmlns="http://www.w3.org/2000/svg"
                            viewBox="0 0 24 24">
                            <path d="M16 12L10 18V6L16 12Z"></path>
                        </svg></button>
                </div>
                <button class="ok-btn" @click="confirm">售卖</button>
            </div>
        </div>
    </div>
</template>
  
<script>
import mixin from '@/js/mixin';
import ListPanel from './ListPanel.vue';
import BasicIntroduction from './BasicIntroduction.vue';


export default {
    components: {
        ListPanel,
        BasicIntroduction,
    },
    props: {
        storeId: {
            type: String,
            required: true,
        },
        itemId: {
            type: String,
            required: true,
        },
    },
    data() {
        return {
            minNumber: 1,
            number: 1,
            maxNumber: 1,
            item: null,
            soldPrice: 0,
            basicInfo: [],
            useInfo: [],
            equipInfo: [],
            handheldInfo: [],
        };
    },
    methods: {
        increment() {
            if (this.number >= this.maxNumber) {
                return;
            }
            this.number++;
        },
        decrement() {
            if (this.number <= this.minNumber) {
                return;
            }
            this.number--;
        },
        async confirm() {
            // 处理卖出请求
            await mixin.myPOSTUrlEncoded('/rest/store/sell',
                {
                    store: this.storeId,
                    itemId: this.itemId,
                    amount: this.number,
                    perPrice: this.soldPrice,
                },
                () => {
                    // 显示提示信息
                    mixin.fadeInfoShow(`卖出${this.number}个${this.item.itemTypeObj.name}`)
                    mixin.fadeInfoShow(`获得${this.number * this.soldPrice}金币`)
                    // 由父组件更新商品列表中该商品的数目
                    this.$emit('onSold', this.number);
                },
            )
        },
        cancel() {
            this.$emit('onCancel');
        }
    },
    async created() {
        this.item = await mixin.myGET("/rest/item/itemDetail", { itemId: this.itemId });
        this.item.image = require(`@/assets/img/${this.item.itemType}.png`);
        // 评估能买多少钱
        this.soldPrice = await mixin.myGET("/rest/store/soldPrice", { store: this.storeId, itemId: this.itemId });
        this.basicInfo = [
            { key: '🔢 数目', value: this.item.itemCount },
            { key: '⭐ 等级', value: this.item.level },
            { key: '💰 售价', value: this.soldPrice },
        ]
        this.maxNumber = this.item.itemCount;
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
                'visionRangeInc': '视野',
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

.button-group span {
    padding-left: 0;
    padding-right: 0;
    margin-top: 20px;
    padding-top: 7px;
    padding-bottom: 7px;
    font-size: 18px;
    color: #000;
    cursor: pointer;
}

.button-group> :not(:last-child) {
    margin-right: 20px;
}

.cancel-btn {
    background-color: #6c757d;
}

.ok-btn {
    background-color: #1165d5;
}
</style>
  