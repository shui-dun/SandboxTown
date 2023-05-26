<template>
    <div class="form" style="text-align: center; margin-top: 18px;">
        <div class="container">
            <div class="row">
                <div class="col-6 item" v-for="item in filteredItems" :key="'grid-' + item.id" @click="clickItem(item)">
                    <div class="item-image" >
                        <h3>{{ item.name }}</h3>
                    </div>
                    <div class="extra">{{ `在线 ${item.peopleNum}` }}</div>
                </div>
            </div>
        </div>
        <div class="btn-group">
            <button class="btn" @click="filterItemsByPage(currentPage - 1)"><svg class="my-svg"
                    xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                    <path
                        d="M13.9142 12.0001L18.7071 7.20718L17.2929 5.79297L11.0858 12.0001L17.2929 18.2072L18.7071 16.793L13.9142 12.0001ZM7 18.0001V6.00008H9V18.0001H7Z">
                    </path>
                </svg></button>
            <button class="btn" @click="filterItemsByPage(currentPage + 1)"><svg class="my-svg"
                    xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                    <path
                        d="M10.0859 12.0001L5.29297 16.793L6.70718 18.2072L12.9143 12.0001L6.70718 5.79297L5.29297 7.20718L10.0859 12.0001ZM17.0001 6.00008L17.0001 18.0001H15.0001L15.0001 6.00008L17.0001 6.00008Z">
                    </path>
                </svg></button>
        </div>
    </div>
</template>
<script>
export default {
    components: {
    },
    data() {
        return {
            items: [
                { id: 1, name: 'Ⅰ', peopleNum: 12 },
                { id: 2, name: 'Ⅱ', peopleNum: 25 },
            ],
            filteredItems: [],
            currentPage: 1,
            itemsPerPage: 4,
        };
    },
    mounted() {
        this.filterItemsByPage(1);
    },
    methods: {
        clickItem(item) {
            // 进入游戏
            this.$router.push({ path: '/game' });
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
            let tmpItems = this.items;
            const start = (this.currentPage - 1) * this.itemsPerPage;
            const end = start + this.itemsPerPage;
            this.filteredItems = tmpItems.slice(start, end);
        },
    },
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
    border: 1px solid #000;
    border-radius: 10px;
    margin-bottom: 10px;
    margin-top: 10px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    width: 100%;
    height: 80px;
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
    fill: #000000;
}

</style>