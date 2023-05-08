<template>
    <button class="button" :class="type" @click="onClick">
        <slot></slot>
    </button>
</template>
  
<script>
export default {
    props: {
        type: {
            type: String,
            default: 'default',
        },
    },
    methods: {
        onClick() {
            this.$emit('click');
        },
    },
};
</script>
  
<style scoped>
.button {
    font-size: 1rem;
    padding: 0.5rem 1rem;
    border-radius: 4px;
    border: none;
    cursor: pointer;
    transition: all 0.3s ease;
}

.button.default {
    background-color: #4caf50;
    color: white;
}

.button.default:hover {
    background-color: #45a049;
}
</style>