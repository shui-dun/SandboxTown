<template>
    <h4>{{ title }}</h4>
    <table class="custom-table">
        <tbody>
            <tr v-for="item in items" :key='item.label' :id="'tr-' + item.label">
                <td>{{ item.show }}</td>
                <td>{{ item.value }}</td>
            </tr>
        </tbody>
    </table>
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
    },
};
</script>
<style scoped>
.custom-table th,
.custom-table td {
    padding: 10px;
    text-align: left;
}
</style>