<template>
    <div>
        <h4 style="">{{ title }}</h4>
        <table class="custom-table">
            <tbody>
                <tr v-for="item in items" :key='item.label' :id="'tr-' + item.label">
                    <td v-if="item.value !== undefined">{{ item.show }}</td>
                    <td v-if="item.value !== undefined">{{ item.value }}</td>
                </tr>
            </tbody>
        </table>
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
    },
};
</script>
<style scoped>
.custom-table {
    margin-top: 20px;
}

.custom-table th,
.custom-table td {
    padding-bottom: 10px;
    padding-right: 30px;
    text-align: left;
    max-width: 200px;
    word-wrap: break-word;
}
</style>