<template>
    <div class="pannel-wrapper">
        <div class="my-pannel">
            <button class="close-btn" @click="close">×</button>
            <div class="nav-group">
                <div v-if="items.length > 1" class="nav nav-pills nav-group-nav">
                    <div v-for="(item, ind) in items" :key="ind" :class="{active: currentInd == ind}" class="nav-link my-nav-item" @click="changeTab(ind)">
                        {{ item }}</div>
                </div>
                <div>
                    <slot :name="currentInd"></slot>
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
            default: () => [],
        },
    },
    components: {
    },
    data() {
        return {
            currentInd: 0
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
        changeTab(tabInd) {
            this.currentInd = tabInd;
        },
    },
};
</script>

<style scoped>
.pannel-wrapper {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 100;
    pointer-events: auto;
}

.my-pannel {
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



.nav-group {
    display: flex;

}

.nav-group .nav-group-nav {
    display: flex;
    flex-direction: column;
    width: 50px;
    margin-right: 20px;
}

.my-nav-item.active {
    background-color: #0079ba;
}
</style>
