<template>
    <div class="grey-bg">
        <div class="popup-panel">
            <div class="popup-panel-header">
                <!-- 物品名称 -->
                <p>{{ (item == null) ? '' : item.itemTypeBean.name }}</p>
            </div>
            <div class="button-group">
                <button class="cancel-btn" @click="cancel()">取消</button>
                <button class="ok-btn" @click="confirm('itembar')">放入物槽</button>
                <button class="ok-btn" @click="confirm('hand')">手持</button>
                <button v-if="canEquip" class="ok-btn" @click="confirm('equip')">装备</button>
                <button v-if="canUse" class="ok-btn" @click="confirm('use')">使用</button>
            </div>
        </div>
    </div>
</template>
  
<script>
import myUtils from '@/js/myUtils';


export default {
    props: {
        itemId: {
            type: String,
            required: true,
        },
    },
    data() {
        return {
            item: null,
            canEquip: false,
            canUse: false,
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
        this.item = await myUtils.myGET("/rest/item/detail", new URLSearchParams({ itemId: this.itemId }));
        this.canEquip = this.item.labels.includes('helmet') || this.item.labels.includes('chest')
            || this.item.labels.includes('leg') || this.item.labels.includes('boots');
        this.canUse = this.item.labels.includes('food') || this.item.labels.includes('usable');
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
}

.popup-panel-header {
    font-size: 24px;
    font-weight: bold;
    color: #333;
    margin-bottom: 20px;
}

.popup-panel-content {
    font-size: 24px;
    font-weight: bold;
    color: #333;
    margin-bottom: 20px;
}

.button-group {
    display: flex;
    width: 100%;
}

.button-group button {
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
  