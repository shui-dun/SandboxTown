<template>
    <div class="popup-window-wrapper">
        <div class="popup-window">
            <div class="header">
                <p>{{ prompt }}</p>
            </div>
            <div class="footer">
                <button class="cancel" @click="cancel">取消</button>
                <button class="confirm" @click="confirm">确定</button>
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
.popup-window-wrapper {
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
    width: 300px;
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

.footer {
    display: flex;
    justify-content: space-between;
    width: 100%;
}

.footer button {
    width: 110px;
    padding: 10px;
    font-size: 16px;
    font-weight: bold;
    color: #fff;
    border: none;
    cursor: pointer;
    border-radius: 5px;
}

.cancel {
    background-color: #999999;
}

.confirm {
    background-color: #1165d5;
}
</style>
  