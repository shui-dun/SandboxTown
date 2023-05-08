<template>
    <div class="signup">
        <h2>注册</h2>
        <InputComponent label="邮箱" @input="setEmail" />
        <InputComponent label="用户名" @input="setUsername" />
        <InputComponent label="密码" type="password" @input="setPassword" />
        <ButtonComponent @click="signUp">注册</ButtonComponent>
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
            email: '',
            username: '',
            password: '',
        };
    },
    methods: {
        setEmail(value) {
            this.email = value;
        },
        setUsername(value) {
            this.username = value;
        },
        setPassword(value) {
            this.password = value;
        },
        signUp() {
            // 注册逻辑
            console.log('注册:', this.email, this.username, this.password);
        },
    },
};
</script>
  
<style scoped>
.signup {
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