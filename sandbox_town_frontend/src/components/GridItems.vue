<template>
    <h4>{{ title }}</h4>
    <div style="margin-bottom: 20px; display:flex;">
        <div class="btn-group">
            <button class="btn btn-outline-primary" @click="filterItemsByCategory('all')">全部</button>
            <button v-for="item in categories" class="btn btn-outline-primary" @click="filterItemsByCategory(item.label)"
                :key="'button-' + item.label">{{ item.prompt }}</button>
        </div>
        <div class="input-group" style="width: 170px;">
            <input type="text" class="form-control" v-model="searchTerm" placeholder="关键词"
                @keyup.enter="filterItemsBySearch()">
            <button class="btn btn-primary" @click="filterItemsBySearch()">查询</button>
        </div>
    </div>
    <div class="container">
        <div class="row" style="width: 400px;">
            <div class="col-3 item" v-for="item in filteredItems" :key="'grid-' + item.id" style="position: relative;">
                <img :src="item.image" :alt="item.name" class="item-image" ref="" />
                <div>{{ item.name }}</div>
                <div v-if="item.extra != undefined">
                    <div class="extra" v-for="(extraItemVal, extraItemKey) in item.extra" :key="'grid-extra-' + item.id + extraItemKey"> {{ extraItemVal }}</div>
                </div>
                <div class="tool-tip">{{ item.description }}</div>
            </div>
        </div>
    </div>
    <div class="btn-group">
        <button class="btn btn-outline-primary" @click="filterItemsByPage(currentPage - 1)">&lt;上一页</button>
        <button class="btn btn-outline-primary" @click="filterItemsByPage(currentPage + 1)">下一页&gt;</button>
    </div>
</template>
<script>
export default {
    props: {
        title: {
            type: String,
            required: true,
        },
        items: {
            type: Array,
            required: true,
        },
        categories: {
            type: Array,
            required: true,
        },
    },
    data() {
        return {
            filterdcategory: 'all',
            searchTerm: '',
            currentPage: 1,
            itemsPerPage: 8,
            filteredItems: [],
        };
    },
    mounted() {
        this.filterItems('all');
    },
    methods: {
        filterItemsByCategory(category) {
            this.filterdcategory = category;
            this.currentPage = 1;
            this.filterItems();
        },
        filterItemsByPage(page) {
            if (page === 0) {
                return;
            }
            if (this.filteredItems.length < this.itemsPerPage && page > this.currentPage) {
                return;
            }
            this.currentPage = page;
            this.filterItems();
        },
        filterItemsBySearch() {
            this.currentPage = 1;
            this.filterItems();
        },
        filterItems() {
            let tmpItems = [];
            // 按照分类筛选
            if (this.filterdcategory === 'all') {
                tmpItems = this.items;
            } else {
                tmpItems = this.items.filter((item) => item.category === this.filterdcategory);
            }
            // 按照搜索词筛选
            if (this.searchTerm !== '') {
                tmpItems = tmpItems.filter((item) => item.name.includes(this.searchTerm));
            }
            // 分页
            const start = (this.currentPage - 1) * this.itemsPerPage;
            const end = start + this.itemsPerPage;
            this.filteredItems = tmpItems.slice(start, end);
        },
    }
};
</script>
<style scoped>
.item {
    text-align: center;
    margin-bottom: 1rem;
    cursor: pointer;
}

.item-image {
    width: 80px;
    height: 80px;
}

.tool-tip {
    display: none;
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
    z-index: 3;
    width: 100px;
}

.extra {
  background-color: #ddd;
  border-radius: 5px;
  font-size: 14px;
}
</style>