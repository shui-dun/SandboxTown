<template>
    <NavGroup @close="$emit('close')">
        <template v-slot:0>
            <div>
                <div style="display: flex; align-items: start; gap: 10px;">
                    <!-- 背包中的物品选择区域 -->
                    <div>
                        <GridPanel title="选择要融合的物品" :items="backpackItems" :labels="labels" @clickGridItem="onClickBackpackItem" />
                    </div>
                    
                    <!-- 被选中用于融合的物品区域 -->
                    <div>
                        <GridPanel title="已选择的物品" :items="selectedItems" :labels="selectedItemLabels" @clickGridItem="removeSelectedItem" />
                    </div>
                </div>
                <div style="margin-top: 20px; display: flex; align-items: center; gap: 10px; padding: 10px; border: 1px solid #ccc; border-radius: 8px;">
                    <div style="margin: 0; background: #f9f9f9; padding: 5px 10px; border-radius: 8px;">
                        可融合为 {{ fusionResult.name }}
                    </div>
                    <div @click="executeFusion" style="cursor: pointer;">
                        <img
                            :src="fusionResult.image"
                            :alt="fusionResult.name"
                            class="fusion-item-image"
                            style="max-width: 50px; max-height: 50px; border-radius: 8px;"
                            :title="fusionResult.description + ' (点击融合)'"
                        />
                    </div>
                    <div>
                        <a
                            href="https://github.com/shui-dun/SandboxTown/blob/master/doc/fusion.md"
                            target="_blank"
                            style="text-decoration: none; color: #333; background: #f9f9f9; padding: 5px 10px; border-radius: 8px;"
                        >
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
            fusionResult: {
                name: '',
                description: '',
                image: require("@/assets/img/PLACEHOLDER.jpg"),
            },
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
            selectedItem.caption = { num: 1 }; // todo 怎么会直接加1呢
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
</style>