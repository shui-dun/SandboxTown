<template>
    <NavGroup @close="$emit('close')">
        <template v-slot:0>
            <div class="fusion-container">
                <h4>🔨 物品融合</h4>
                <div class="panels-container">
                    <!-- 背包中的物品选择区域 -->
                    <GridPanel title="选择要融合的物品" :items="backpackItems" :labels="labels" @clickGridItem="onClickBackpackItem" />
                    
                    <!-- 被选中用于融合的物品区域 -->
                    <GridPanel title="已选择的物品" :items="selectedItems" :labels="selectedItemLabels" @clickGridItem="removeSelectedItem" />
                </div>
                
                <div class="bottom-row">
                    <!-- 可以被融合得到的物品 -->
                    <div v-if="fusionResult" class="fusion-result">
                        <h5>可融合为：</h5>
                        <div class="result-item" @click="executeFusion">
                            <img :src="fusionResult.image" :alt="fusionResult.name" class="fusion-item-image" />
                            <div class="item-name">{{ fusionResult.name }}</div>
                            <div class="caption">{{ fusionResult.description }}</div>
                        </div>
                    </div>
                    
                    <!-- 融合公式链接 -->
                    <div class="fusion-formula">
                        <a href="https://github.com/shui-dun/SandboxTown/blob/master/doc/fusion.md" target="_blank">
                            📖 查看融合公式
                        </a>
                    </div>
                </div>
            </div>
        </template>
    </NavGroup>
</template>

<script>
import GridPanel from './GridPanel.vue';
import NavGroup from './NavGroup.vue';
import mixin from '@/js/mixin.js';
import { ITEM_LABELS } from '@/js/constants.js';

export default {
    components: {
        GridPanel,
        NavGroup,
    },
    emits: ['close'],
    data() {
        return {
            backpackItems: [],
            selectedItems: [],
            fusionResult: null,
            labels: ITEM_LABELS,
        };
    },
    computed: {
        selectedItemLabels() {
            return [ITEM_LABELS[0]]; // 只返回 ALL 标签
        }
    },
    mounted() {
        this.refreshBackpack();
    },
    methods: {
        refreshBackpack() {
            mixin.myGET('/rest/item/listMyItemsInBackpack', null, (data) => {
                let itemLst = [];
                data.forEach((element) => {
                    let item = {};
                    item.id = element.id;
                    item.name = element.itemTypeObj.name;
                    item.caption = { num: element.itemCount };
                    item.image = require(`@/assets/img/${element.itemType}.png`);
                    item.labels = [];
                    let isEquipment = false;
                    for (let label of element.itemTypeObj.labels) {
                        if ((label === 'HELMET' || label === 'CHEST' || label === 'LEG' || label === 'BOOTS') && !isEquipment) {
                            isEquipment = true;
                            item.labels.push('EQUIPMENT');
                        } else {
                            item.labels.push(label);
                        }
                    }
                    item.description = element.itemTypeObj.description;
                    item.content = element;
                    itemLst.push(item);
                });
                this.backpackItems = itemLst;
            });
        },
        onClickBackpackItem(item) {
            // Add item to selected items
            const selectedItem = {...item};
            selectedItem.caption = { num: 1 }; // Always add 1 item for fusion
            this.selectedItems.push(selectedItem);
            
            // Update fusion result
            this.checkFusion();
        },
        removeSelectedItem(item) {
            const index = this.selectedItems.findIndex(i => i.id === item.id);
            if (index !== -1) {
                this.selectedItems.splice(index, 1);
                this.checkFusion();
            }
        },
        async checkFusion() {
            if (this.selectedItems.length === 0) {
                this.fusionResult = null;
                return;
            }

            // Convert array to comma-separated string
            const itemIdsString = this.selectedItems.map(item => item.id).join(',');

            try {
                const response = await mixin.myPOST('/rest/fusion/check', 
                    new URLSearchParams({
                        itemIds: itemIdsString
                    })
                );
                if (response && response.resultType) {
                    this.fusionResult = {
                        name: response.resultTypeName,
                        description: response.resultTypeDescription,
                        image: require(`@/assets/img/${response.resultType}.png`)
                    };
                } else {
                    this.fusionResult = null;
                }
            } catch (error) {
                console.error('Failed to check fusion:', error);
                this.fusionResult = null;
            }
        },
        async executeFusion() {
            if (!this.fusionResult) return;

            // Convert array to comma-separated string
            const itemIdsString = this.selectedItems.map(item => item.id).join(',');

            try {
                await mixin.myPOST('/rest/fusion/execute',
                    new URLSearchParams({
                        itemIds: itemIdsString
                    })
                );
                mixin.fadeInfoShow(`成功融合得到 ${this.fusionResult.name}`);
                // Reset selection and refresh backpack
                this.selectedItems = [];
                this.fusionResult = null;
                this.refreshBackpack();
            } catch (error) {
                console.error('Failed to execute fusion:', error);
                mixin.fadeInfoShow('融合失败');
            }
        }
    }
};
</script>

<style scoped>
.fusion-container {
    max-width: 1200px;
    padding: 20px;
}

.panels-container {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    margin-bottom: 20px;
}

.panels-container > * {
    flex: 1;
}

.bottom-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 10px;
    background-color: #f0f0f0;
    border-radius: 5px;
}

.fusion-result {
    text-align: center;
    flex: 1;
}

.result-item {
    cursor: pointer;
    padding: 10px;
    border-radius: 5px;
    background-color: #fff;
    transition: background-color 0.2s;
}

.result-item:hover {
    background-color: #e0e0e0;
}

.fusion-item-image {
    width: 64px;
    height: 64px;
}

.item-name {
    margin-top: 5px;
    font-weight: bold;
}

.fusion-formula {
    margin-left: 20px;
    padding-top: 30px;
}

.fusion-formula a {
    text-decoration: none;
    color: #1165d5;
    padding: 10px;
    background-color: #fff;
    border-radius: 5px;
    display: inline-block;
}

f.fusion-formula a:hover {
    background-color: #e0e0e0;
}

h4 {
    margin-bottom: 20px;
}

h5 {
    margin-bottom: 10px;
}

.caption {
    background-color: #ddd;
    border-radius: 5px;
    margin-top: 5px;
    font-size: 14px;
    padding: 2px 5px;
}
</style>