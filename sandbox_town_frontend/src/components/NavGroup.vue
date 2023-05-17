<template>
    <div class="nav-group-wrapper">
        <div class="nav-group">
            <button class="close-btn" @click="close">×</button>
            <div class="my-pannel">
                <div v-if="items.length > 1" class="nav nav-pills my-pannel-nav">
                    <div v-for="(item, ind) in items" :key="'nav-tab-' + item.name" class="nav-link my-nav-item"
                        @click="changeTab('tab' + ind)">{{ item.label }}</div>
                </div>
                <div>
                    <div v-for="(item, ind) in items" :key="'nav-item-' + item.name">
                        <component :is="item.name" v-if="currentTab === 'tab' + ind" v-bind="item.props" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import InfoList from './InfoList.vue';
import GridItems from './GridItems.vue';

export default {
    props: {
        items: {
            type: Array,
            required: true,
        },
    },
    components: {
        InfoList,
        GridItems
    },
    data() {
        return {
            currentTab: 'tab0'
        };
    },
    mounted() {

    },
    computed: {
    },
    methods: {
        close() {
            this.$emit('close');
        },
        changeTab(tabname) {
            this.currentTab = tabname;
        },
    },
};
</script>

<style scoped>
.nav-group-wrapper {
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

.nav-group {
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


.my-nav-item {
    cursor: pointer;
    background-color: #f7d7c4;
    margin-top: 7px;
    margin-bottom: 7px;
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
