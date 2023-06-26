<template>
    <div class="popup-window-wrapper">
        <div class="popup-window">
            <div class="header">
                <p>{{ prompt }}</p>
            </div>
            <div class="content">
                <button @click="decrement"><svg width="50" height="50" xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 24 24">
                        <path d="M8 12L14 6V18L8 12Z"></path>
                    </svg></button>
                <span>{{ number }}</span>
                <button @click="increment"><svg width="50" height="50" xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 24 24">
                        <path d="M16 12L10 18V6L16 12Z"></path>
                    </svg></button>
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
        minNumber: {
            type: Number,
            default: 1,
        },
        maxNumber: {
            type: Number,
            required: true,
        },
        prompt: {
            type: String,
            default: '请选择数目',
        },
    },
    data() {
        return {
            number: this.minNumber,
        };
    },
    methods: {
        increment() {
            if (this.number >= this.maxNumber) {
                return;
            }
            this.number++;
        },
        decrement() {
            if (this.number <= this.minNumber) {
                return;
            }
            this.number--;
        },
        cancel() {
            this.$emit('onCancel');
        },
        confirm() {
            this.$emit('onConfirm', this.number);
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

.content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    /* width: 100%; */
    margin-bottom: 20px;
}

.content button {
    background-color: transparent;
    border: none;
    font-size: 35px;
    cursor: pointer;
}

.content span {
    font-size: 30px;
    font-weight: bold;
    color: #333;
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
}</style>
  