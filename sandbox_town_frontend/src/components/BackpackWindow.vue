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
            <div class="container">
                <div class="row">
                    <div class="col-md-3">
                        <b-nav vertical>
                            <b-nav-item @click="changeTab('basicInfo')">基础信息</b-nav-item>
                            <b-nav-item @click="changeTab('items')">物品栏</b-nav-item>
                        </b-nav>
                    </div>
                    <div class="col-md-9">
                        <div v-if="currentTab === 'basicInfo'">
                            <h3>基础信息</h3>
                            <!-- ... -->
                        </div>
                        <div v-else-if="currentTab === 'items'">
                            <h3>物品栏</h3>
                            <!-- <b-button-group> -->
                                <!-- <b-button variant="outline-primary" @click="filterItems('all')">全部</b-button> -->
                                <!-- <b-button variant="outline-primary" @click="filterItems('food')">食品</b-button> -->
                                <!-- <b-button variant="outline-primary" @click="filterItems('equipment')">装备</b-button> -->
                                <!-- <b-button variant="outline-primary" @click="filterItems('item')">物品</b-button> -->
                            <!-- </b-button-group> -->
                            <!-- <b-container fluid> -->
                                ...
                            <!-- </b-container> -->
                            <!-- <b-pagination v-model="currentPage" :total-rows="filteredItems.length" :per-page="itemsPerPage" -->
                                <!-- aria-controls="items-grid"></b-pagination> -->
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
            // ...
            currentTab: 'basicInfo',
            // currentPage: 1,
            // itemsPerPage: 9,
            // filteredItems: [],
        };
    },
    // computed: {
    //     paginatedItems() {
    //         const start = (this.currentPage - 1) * this.itemsPerPage;
    //         const end = start + this.itemsPerPage;
    //         return this.filteredItems.slice(start, end);
    //     },
    // },
    methods: {
        // ...
        // filterItems(category) {
        //     if (category === 'all') {
        //         this.filteredItems = this.player.items;
        //     } else {
        //         this.filteredItems = this.player.items.filter((item) => item.category === category);
        //     }
        //     this.currentPage = 1;
        // },
        closeComponent() {
            this.$emit('close');
        },
        changeTab(tabname) {
            this.currentTab = tabname;
        }
    },
    // mounted() {
    //     this.filterItems('all');
    // },
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
}

.player-info {
    background-color: #fff;
    border-radius: 5px;
    padding: 1rem;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}

.close-btn {
    position: absolute;
    top: 10px;
    right: 10px;
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
}

.item {
    text-align: center;
    margin-bottom: 1rem;
}

.item-image {
    width: 100%;
    height: auto;
}
</style>
