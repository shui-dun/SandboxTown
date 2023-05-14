
<template>
    <div class="mt-4" id="login-form">
        <custom-input id="emailOrUsername" label="邮箱或用户名" @input="onInput" />
        <custom-input id="password" label="密码" type="password" @input="onInput" />
        <button class="btn btn-primary" @click="onLogin">登录</button>
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
            emailOrUsername: '',
            password: '',
        };
    },
    methods: {
        onInput(value) {
            this.emailOrUsername = value;
        },
        onLogin() {
            // 处理登录逻辑
            this.$router.push({ path: '/game'});
        },
    },
};
</script>

<style scoped> 
#login-form {
    width: 80%;
    margin: 0 auto;
}
</style>