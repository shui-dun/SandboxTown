<template>
    <div class="item-bar">
        <div v-for="(item, index) in items" :key="index" class="item" :class="{ selected: handheldItemIndex === index }"
            @click="selectItem(index, item)" @contextmenu.prevent="emitClickEvent(index, item)">
            <img :src="item.image" alt="item" />
        </div>
    </div>
    <ItemDetail v-if="selectedItemIndex !== null" :itemId="items[selectedItemIndex].id" @onConfirm="confirm" @onCancel="cancel" />
</template>
  
<script>
import mixin from '@/js/mixin';
import ItemDetail from './ItemDetail.vue';

export default {
    components: {
        ItemDetail,
    },
    data() {
        return {
            items: [],
            selectedItemIndex: null,
            handheldItemIndex: null,
        };
    },
    methods: {
        // 点击左键，选中（手持）物品
        selectItem(index, item) {
            // 如果是空物品，不显示
            if (item.id == null) {
                return;
            }
            this.handheldItemIndex = index;
            // TODO: 手持物品
        },
        // 点击右键，查看物品信息
        emitClickEvent(index, item) {
            // 如果是空物品，不显示
            if (item.id == null) {
                return;
            }
            this.selectedItemIndex = index;
            console.log(this.items[this.selectedItemIndex])
        },
        confirm() {
            this.selectedItemIndex = null;
        },
        cancel() {
            this.selectedItemIndex = null;
        },
    },
    mounted() {
        // 向后端请求物品栏物品数据
        mixin.myGET('/rest/item/listMyItemsInItemBar', null, (data) => {
            this.items = data;
            // 设置图片
            this.items.forEach((item) => {
                item.image = require(`@/assets/img/${item.itemType}.png`);
            });
            // 设置手持物品
            this.handheldItemIndex = this.items.findIndex((item) => item.position == "HANDHELD");
            // 剩下的空位用空物品填充
            for (let i = this.items.length; i < 6; i++) {
                this.items.push({ image: require("@/assets/img/PLACEHOLDER2.png") });
            }
        });
    },
};
</script>
  
<style scoped>
.item-bar {
    display: flex;
    justify-content: flex-end;
    position: fixed;
    top: 2%;
    right: 2%;
    z-index: 50;
}

.item {
    width: 50px;
    height: 50px;
    margin-left: 10px;
    border: 4px solid transparent;
    transition: border-color 0.3s ease;
    border-radius: 10px;
    cursor: pointer;
}

.item img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 10px;
    background-color: white;
}

.item.selected {
    border-color: black;
}
</style>
  