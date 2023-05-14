<template>
    <!-- 组件的左侧是一个导航栏，包含2个栏目：基础信息、物品栏 -->
    <!-- 组件的右侧显示详细内容-->
    <!-- 当点击导航栏的基础信息时，详细内容区域显示玩家的基础信息，包括用户名、金钱数目、经验值、等级、饥饿值、血量 -->
    <!-- 当点击导航栏的物品栏时，详细内容区域显示玩家拥有的物品 -->
    <!-- 物品展示区域从上往下包含3部分，上部分是物品类别选择区域，包含全部、食品、装备、物品这几个类别 -->
    <!-- 中间部分是物品网格，请使用网格布局展示玩家的物品，每一格物品包含两部分，上侧展示物品的图片，下侧展示物品的名称，并且当光标悬浮在物品上时，显示物品的详细介绍。点击对应的类别将显示对应类别的物品。 -->
    <!-- 下部分是翻页栏，包含上一页和下一页两个按钮，点击即可实现翻页功能 -->
    <!-- 这个组件我想要始终位于页面的正中央，并且是在页面的最上层，不管下层的页面元素如何变化，这个组件都不受影响 -->
    <!-- 最后，请在组下右上角增添一个关闭按钮 -->
    <div class="player-info-wrapper">
        <div class="player-info">
            <button class="close-btn" @click="closeComponent">×</button>
            <div class="my-pannel">
                <div class="nav nav-pills my-pannel-nav">
                    <div class="nav-link my-nav-item" @click="changeTab('basicInfo')">基础信息</div>
                    <div class="nav-link my-nav-item" @click="changeTab('items')">物品栏</div>
                </div>
                <div>
                    <div v-if="currentTab === 'basicInfo'">
                        <h4>🔍 基础信息</h4>
                        <table class="custom-table">
                            <tbody>
                                <tr>
                                    <td>👨‍💼 用户名</td>
                                    <td>{{ player.username }}</td>
                                </tr>
                                <tr>
                                    <td>💰 金钱数目</td>
                                    <td>{{ player.money }}</td>
                                </tr>
                                <tr>
                                    <td>🍾 经验值</td>
                                    <td>{{ player.exp }}</td>
                                </tr>
                                <tr>
                                    <td>⬆️ 等级</td>
                                    <td>{{ player.level }}</td>
                                </tr>
                                <tr>
                                    <td>🥪 饥饿值</td>
                                    <td>{{ player.hunger }}</td>
                                </tr>
                                <tr>
                                    <td>🩸 血量</td>
                                    <td>{{ player.hp }}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div v-else-if="currentTab === 'items'">
                        <h4>🎁 物品栏</h4>
                        <div style="margin-bottom: 20px; display:flex;">
                            <div class="btn-group">
                                <button class="btn btn-outline-primary" @click="filterItemsByCategory('all')">全部</button>
                                <button class="btn btn-outline-primary" @click="filterItemsByCategory('food')">食品</button>
                                <button class="btn btn-outline-primary" @click="filterItemsByCategory('pet')">宠物</button>
                                <button class="btn btn-outline-primary" @click="filterItemsByCategory('item')">物品</button>
                            </div>
                            <div class="input-group" style="width: 170px;">
                                <input type="text" class="form-control" v-model="searchTerm" placeholder="关键词" @keyup.enter="filterItemsBySearch()">
                                <button class="btn btn-primary" @click="filterItemsBySearch()">查询</button>
                            </div>
                        </div>
                        <div class="container">
                            <div class="row" style="width: 400px;">
                                <div class="col-3 item" v-for="item in filteredItems" :key="item.id"
                                    style="position: relative;">
                                    <img :src="item.image" :alt="item.name" class="item-image" ref="" />
                                    <div>{{ item.name }}</div>
                                    <div class="tool-tip">{{ item.description }}</div>
                                </div>
                            </div>
                        </div>
                        <div class="btn-group">
                            <button class="btn btn-outline-primary"
                                @click="filterItemsByPage(currentPage - 1)">&lt;上一页</button>
                            <button class="btn btn-outline-primary"
                                @click="filterItemsByPage(currentPage + 1)">下一页&gt;</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            currentTab: 'basicInfo',
            player: {
                username: 'Player1',
                money: 1000,
                exp: 200,
                level: 5,
                hunger: 50,
                hp: 100,
                items: [
                    { id: 1, name: '面包', image: require("@/assets/img/bread.png"), category: 'food', description: '具有松软的质地和丰富的口感' },
                    { id: 2, name: '锯子', image: require("@/assets/img/saw.png"), category: 'item', description: '用来锯木头' },
                    { id: 3, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火' },
                    { id: 4, name: '猫咪', image: require("@/assets/img/cat.png"), category: 'pet', description: '常见的家养宠物，具有柔软的毛发、灵活的身体和独立的性格' },
                    { id: 5, name: '面包', image: require("@/assets/img/bread.png"), category: 'food', description: '具有松软的质地和丰富的口感' },
                    { id: 6, name: '锯子', image: require("@/assets/img/saw.png"), category: 'item', description: '用来锯木头' },
                    { id: 7, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火' },
                    { id: 8, name: '猫咪', image: require("@/assets/img/cat.png"), category: 'pet', description: '常见的家养宠物，具有柔软的毛发、灵活的身体和独立的性格' },
                    { id: 9, name: '面包', image: require("@/assets/img/bread.png"), category: 'food', description: '具有松软的质地和丰富的口感' },
                    { id: 10, name: '锯子', image: require("@/assets/img/saw.png"), category: 'item', description: '用来锯木头' },
                    { id: 11, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火' },
                    { id: 11, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火' },
                    { id: 11, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火' },
                    { id: 11, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火' },
                    { id: 11, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火' },
                    { id: 11, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火' },
                    { id: 11, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火' },
                    { id: 11, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火' },
                    { id: 11, name: '木材', image: require("@/assets/img/wood.png"), category: 'item', description: '建筑的材料，也可处于烤火' },

                ],
            },
            filterdcategory: 'all',
            searchTerm: '',
            currentPage: 1,
            itemsPerPage: 8,
            filteredItems: [],
        };
    },
    computed: {
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
                tmpItems = this.player.items;
            } else {
                tmpItems = this.player.items.filter((item) => item.category === this.filterdcategory);
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
        closeComponent() {
            this.$emit('close');
        },
        changeTab(tabname) {
            this.currentTab = tabname;
        },
    },
    mounted() {
        this.filterItems('all');
    },
};
</script>

<style scoped>
.player-info-wrapper {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 9999;
    pointer-events: auto;
}

.player-info {
    background-color: #fff;
    border-radius: 5px;
    padding: 1rem;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}

.close-btn {
    position: absolute;
    top: 0px;
    right: 10px;
    background: none;
    border: none;
    font-size: 3.5rem;
    cursor: pointer;
}

.item {
    text-align: center;
    margin-bottom: 1rem;
    cursor: pointer;
}

.item-image {
    width: 80px;
    height: 80px;
}

.custom-table th,
.custom-table td {
    padding: 10px;
    text-align: left;
}

.my-nav-item {
    cursor: pointer;
    background-color: #f7d7c4;
    margin-top: 7px;
    margin-bottom: 7px;
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

.my-pannel {
    display: flex;

}

.my-pannel .my-pannel-nav {
    display: flex;
    flex-direction: column;
    width: 50px;
    margin-right: 20px;
}
</style>
