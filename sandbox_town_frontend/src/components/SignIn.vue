<template>
    <div class="signin">
        <h2>登录</h2>
        <InputComponent label="邮箱或用户名" @input="setUsername" />
        <InputComponent label="密码" type="password" @input="setPassword" />
        <ButtonComponent @click="signIn">登录</ButtonComponent>
    </div>
</template>
  
<script>
import InputComponent from './InputComponent.vue';
import ButtonComponent from './ButtonComponent.vue';

export default {
    components: {
        InputComponent,
        ButtonComponent,
    },
    data() {
        return {
            username: '',
            password: '',
        };
    },
    methods: {
        setUsername(value) {
            this.username = value;
        },
        setPassword(value) {
            this.password = value;
        },
        signIn() {
            // 登录逻辑
            console.log('登录:', this.username, this.password);
        },
    },
};
</script>
  
<style scoped>
.signin {
    width: 100%;
    max-width: 400px;
    margin: 0 auto;
    padding: 1rem;
    border: 1px solid #ccc;
    border-radius: 4px;
    background-color: #f9f9f9;
}

h2 {
    text-align: center;
    margin-bottom: 1rem;
}
</style>