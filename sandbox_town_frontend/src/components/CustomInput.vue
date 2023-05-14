<template>
    <div class="mb-3">
        <label :for="id" class="form-label">{{ label }}</label>
        <input :id="id" :type="type" class="form-control" @input="onInput" />
    </div>
</template>
  
<script>
export default {
    props: {
        id: {
            type: String,
            required: true,
        },
        label: {
            type: String,
            required: true,
        },
        type: {
            type: String,
            default: 'text',
        },
    },
    methods: {
        onInput(event) {
            this.$emit('input', event.target.value);
        },
    },
};
</script>
  