<template>
    <div class="grey-bg">
        <div class="popup-window">
            <div class="header">
                <p>{{ prompt }}</p>
            </div>
            <div class="button-group">
                <button @click="cancel">取消</button>
                <button @click="confirm">确定</button>
            </div>
        </div>
    </div>
</template>
  
<script>
export default {
    props: {
        prompt: {
            type: String,
            required: true,
        },
    },
    data() {
        return {

        };
    },
    methods: {
        cancel() {
            this.$emit('onCancel');
        },
        confirm() {
            this.$emit('onConfirm');
        },
    },
};
</script>
  
<style scoped>
.grey-bg {
    position: fixed;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 200;
    background-color: rgba(0, 0, 0, 0.5);
}

.popup-window {
    display: flex;
    flex-direction: column;
    align-items: center;
    background-color: #f0f0f0;
    border-radius: 10px;
    padding: 20px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.header {
    font-size: 24px;
    font-weight: bold;
    color: #333;
    margin-bottom: 20px;
}

.button-group {
    display: flex;
    justify-content: space-between;
    width: 100%;
}

.button-group button {
    width: 80px;
    padding: 5px;
    font-size: 14px;
    color: #fff;
    border: none;
    cursor: pointer;
    border-radius: 5px;
    background-color: #6c757d;
}

.button-group button:not(:last-child) {
    margin-right: 20px;
}

</style>
  