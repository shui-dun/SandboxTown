<template>
    <div class="mt-4" id="register-form">
        <custom-input id="email" label="邮箱" @input="onInput" />
        <custom-input id="username" label="用户名" @input="onInput" />
        <custom-input id="password" label="密码" type="password" @input="onInput" />
        <button class="btn btn-primary" @click="onRegister">注册</button>
    </div>
</template>
  
<script>
import CustomInput from './CustomInput.vue';

export default {
    components: {
        CustomInput,
    },
    data() {
        return {
            email: '',
            username: '',
            password: '',
        };
    },
    methods: {
        onInput(value) {
            this.email = value;
        },
        onRegister() {
            // 处理注册逻辑
        },
    },
};
</script>
  
<style scoped>
#register-form {
    width: 80%;
    margin: 0 auto;
}
</style>