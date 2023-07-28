<template>
    <div class="row custom-table">
        <div :class="{ 'col-6': item.value.toString().length < 10, 'col-12': item.value.toString().length >= 10 }"
            v-for="item in filteredItems" :key='item.key' :id="'tr-' + item.key">
            <div class="row">
                <div style="display: flex;">
                    <div style="margin-right: 0px; width: 100px;">{{ item.key }}</div>
                    <div>{{ item.value }}</div>
                </div>
            </div>
        </div>
    </div>
</template>
<script>
export default {
    props: {
        items: {
            type: Array,
            required: true,
        },
    },
    computed: {
        // computed定义的方法，只有相关依赖发生改变时才会去调用
        filteredItems() {
            // 过滤掉value不存在的项
            return this.items.filter((item) => item.value != null);
        },
    },
};
</script>
<style scoped>
.custom-table {
    margin-top: 5px;
    margin-bottom: 5px;
}

.custom-table div {
    word-wrap: break-word;
    margin-bottom: 3px;
}
</style>