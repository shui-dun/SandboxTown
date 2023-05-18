<template>
    <h4>{{ title }}</h4>
    <div style="margin-bottom: 20px; display:flex;">
        <div class="btn-group">
            <button class="btn btn-outline-primary" @click="filterItemsByCategory('all')">全部</button>
            <button v-for="item in categories" class="btn btn-outline-primary" @click="filterItemsByCategory(item.label)"
                :key="'button-' + item.label">{{ item.prompt }}</button>
        </div>
        <div class="input-group" style="width: 120px;">
            <input type="text" class="form-control" v-model="searchTerm" placeholder="关键词"
                @keyup.enter="filterItemsBySearch()">
            <button class="btn btn-outline-primary" @click="filterItemsBySearch()"><svg class="my-svg" xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24">
                    <path
                        d="M11 2C15.968 2 20 6.032 20 11C20 15.968 15.968 20 11 20C6.032 20 2 15.968 2 11C2 6.032 6.032 2 11 2ZM11 18C14.8675 18 18 14.8675 18 11C18 7.1325 14.8675 4 11 4C7.1325 4 4 7.1325 4 11C4 14.8675 7.1325 18 11 18ZM19.4853 18.0711L22.3137 20.8995L20.8995 22.3137L18.0711 19.4853L19.4853 18.0711Z">
                    </path>
                </svg></button>
        </div>
    </div>
    <div class="container">
        <div class="row" style="width: 450px;">
            <div class="col-3 item" v-for="item in filteredItems" :key="'grid-' + item.id" @click="clickItem(item)">
                <img :src="item.image" :alt="item.name" class="item-image" ref="" />
                <div>{{ item.name }}</div>
                <div v-if="item.extra != undefined">
                    <div class="extra" v-for="(extraItemVal, extraItemKey) in item.extra"
                        :key="'grid-extra-' + item.id + extraItemKey"> {{ extraItemVal }}</div>
                </div>
                <div class="tool-tip">{{ item.description }}</div>
            </div>
        </div>
    </div>
    <div class="btn-group">
        <button class="btn btn-outline-primary" @click="filterItemsByPage(currentPage - 1)"><svg class="my-svg"
                xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                <path
                    d="M13.9142 12.0001L18.7071 7.20718L17.2929 5.79297L11.0858 12.0001L17.2929 18.2072L18.7071 16.793L13.9142 12.0001ZM7 18.0001V6.00008H9V18.0001H7Z">
                </path>
            </svg>上一页</button>
        <button class="btn btn-outline-primary" @click="filterItemsByPage(currentPage + 1)">下一页<svg class="my-svg"
                xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                <path
                    d="M10.0859 12.0001L5.29297 16.793L6.70718 18.2072L12.9143 12.0001L6.70718 5.79297L5.29297 7.20718L10.0859 12.0001ZM17.0001 6.00008L17.0001 18.0001H15.0001L15.0001 6.00008L17.0001 6.00008Z">
                </path>
            </svg></button>
    </div>
</template>
<script>
export default {
    emits: ['clickGridItem'],
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
            if (page > this.currentPage && (this.filteredItems.length < this.itemsPerPage ||
                this.filteredItems.at(-1).id == this.items.at(-1).id)) {
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
        clickItem(item) {
            this.$emit('clickGridItem', item);
        }
    }
};
</script>
<style scoped>
.item {
    text-align: center;
    margin-bottom: 1rem;
    cursor: pointer;
    position: relative;
}

.item-image {
    width: 80px;
    height: 80px;
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
    margin-bottom: 3px;
    font-size: 14px;
}

.my-svg {
    width: 20px;
    height: 20px;
    fill: #1476fa;
}

button:hover .my-svg {
    fill: white;
}
</style>