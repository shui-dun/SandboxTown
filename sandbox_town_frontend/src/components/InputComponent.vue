<template>
    <div class="input-wrapper">
        <label>{{ label }}</label>
        <input :type="type" v-model.trim="inputValue" @input="onInput" />
    </div>
</template>
  
<script>
export default {
    props: {
        label: String,
        type: {
            type: String,
            default: 'text',
        },
    },
    data() {
        return {
            inputValue: '',
        };
    },
    methods: {
        onInput() {
            this.$emit('input', this.inputValue);
        },
    },
};
</script>
  
<style scoped>
.input-wrapper {
    display: flex;
    flex-direction: column;
    margin-bottom: 1rem;
}

label {
    margin-bottom: 0.5rem;
}

input {
    padding: 0.5rem;
    border: 1px solid #ccc;
    border-radius: 4px;
    font-size: 1rem;
}
</style>