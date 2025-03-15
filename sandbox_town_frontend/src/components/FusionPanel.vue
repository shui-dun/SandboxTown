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

const defaultFusionResult = {
    name: '',
    description: '',
    image: require("@/assets/img/PLACEHOLDER.jpg"),
};

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
            fusionResult: defaultFusionResult,
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
            if (item.caption.num <= 0) return;
            item.caption.num--;
            let selectedItem = this.selectedItems.find(i => i.id === item.id);
            if (selectedItem) {
                selectedItem.caption.num++;
            } else {
                selectedItem = { ...item};
                selectedItem.caption = { num: 1 };
                this.selectedItems.push(selectedItem);
            }
            this.checkFusion();
        },
        removeSelectedItem(item) {
            let backpackItem = this.backpackItems.find(i => i.id === item.id);
            backpackItem.caption.num++;
            item.caption.num--;
            if (item.caption.num === 0) {
                this.selectedItems = this.selectedItems.filter(i => i.id !== item.id);
            }
            this.checkFusion();
        },

        async checkFusion() {
            if (this.selectedItems.length === 0) {
                this.fusionResult = defaultFusionResult;
                return;
            }

            // 准备请求数据: Map<String, Integer>
            const items = {};
            this.selectedItems.forEach(item => {
                items[item.id] = item.caption.num;
            });

            // 调用后端检查融合接口
            await mixin.myPOST('/rest/fusion/check', 
                new URLSearchParams({ request: JSON.stringify(items) }), 
                async (data) => {
                    if (data == null) {
                        console.error('融合检查失败');
                        this.fusionResult = defaultFusionResult;
                    }
                    // 通过后端接口获取物品类型详细信息
                    const itemTypeDetail = await mixin.myGET('/item/itemTypeDetail', 
                        new URLSearchParams({ itemType: data.resultItem }));
                    if (itemTypeDetail) {
                        this.fusionResult = {
                            name: itemTypeDetail.name,
                            image: require(`@/assets/img/${data.resultItem}.png`),
                            description: itemTypeDetail.description
                        };
                    }
                });
        },

        async executeFusion() {
            if (this.fusionResult.name === '') {
                return;
            }

            // 准备请求数据: Map<String, Integer>
            const items = {};
            this.selectedItems.forEach(item => {
                items[item.id] = item.caption.num;
            });

            // 调用后端执行融合接口
            await mixin.myPOST('/rest/fusion/execute', 
                new URLSearchParams({ request: JSON.stringify(items) }), 
                () => {
                    this.selectedItems = [];
                    this.fusionResult = defaultFusionResult;
                    this.refreshBackpack();
                });
        }
    }
};
</script>

<style scoped>
</style>