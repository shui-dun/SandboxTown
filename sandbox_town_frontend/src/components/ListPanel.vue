<template>
    <div>
        <table class="custom-table">
            <tbody>
                <tr v-for="item in items" :key='item.key' :id="'tr-' + item.key">
                    <td v-if="item.value !== undefined">{{ item.key }}</td>
                    <td v-if="item.value !== undefined">{{ item.value }}</td>
                </tr>
            </tbody>
        </table>
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
};
</script>
<style scoped>
.custom-table {
    margin-top: 20px;
    table-layout: fixed;
}

.custom-table th,
.custom-table td {
    padding-bottom: 10px;
    padding-right: 30px;
    text-align: left;
    word-wrap: break-word;
}

.custom-table td:first-child  {
    min-width: 120px;
}

.custom-table td:last-child  {
    min-width: 120px;
    padding-right: 30px;
}
</style>