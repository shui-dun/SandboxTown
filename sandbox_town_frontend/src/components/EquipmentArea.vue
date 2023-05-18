<template>
    <h4>{{ title }}</h4>
    <div class="equipment-area">
        <table class="custom-table">
            <tbody>
                <tr v-for="item in listItems" :key='item.label' :id="'tr-' + item.label">
                    <td>{{ item.show }}</td>
                    <td>{{ item.value }}</td>
                </tr>
            </tbody>
        </table>
        <div class="equipment-grid">
            <div class="container">
                <div class="row" style="width: 250px;">
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
</template>
<script>
export default {
    emits: ['onClickGridItem'],
    props: {
        title: {
            type: String,
            required: true,
        },
        listItems: {
            type: Array,
            required: true,
        },
        equipmentItems: {
            type: Object,
            required: true,
        },
    },
    methods: {
        clickGridItem(itemKey, item) {
            if (item.name) {
                this.$emit('onClickGridItem', { 'itemKey': itemKey, 'item': item })
            }
        },
    },
};
</script>
<style scoped>
.equipment-area {
    display: flex;
}

.custom-table th,
.custom-table td {
    padding: 10px;
    text-align: left;
}


.item {
    text-align: center;
    margin-bottom: 1rem;
    cursor: pointer;
    position: relative;
}

.item-image {
    width: 80px;
    height: 80px;
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

.extra {
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