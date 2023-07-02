<template>
    <div>
        <NavGroup :items="componentItems" @close="$emit('close')">
            <template v-slot:0>
                <StoreBuy :storeId="storeId" />
            </template>
            <template v-slot:1>
                <StoreSold :storeId="storeId" />
            </template>
        </NavGroup>

    </div>
</template>

<script>
import NavGroup from './NavGroup.vue';
import StoreBuy from './StoreBuy.vue';
import StoreSold from './StoreSold.vue';

export default {
    props: {
        storeId: {
            type: String,
            default: '',
        },
    },
    components: {
        NavGroup,
        StoreBuy,
        StoreSold,
    },
    data() {
        return {
            componentItems: ['买入', '卖出'],
        };
    },
    async mounted() {

    },
    computed: {
    },
    methods: {
    },
};
</script>

<style scoped></style>
