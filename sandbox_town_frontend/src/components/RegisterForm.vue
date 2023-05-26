<template>
    <div class="mt-4" id="register-form">
        <div class="mb-3">
            <label class="form-label">用户名</label>
            <input v-model="username" type="text" class="form-control" />
        </div>
        <div class="mb-3">
            <label class="form-label">密码</label>
            <input v-model="password" type="password" class="form-control" />
        </div>
        <div class="mb-3">
            <label class="form-label">再次输入密码</label>
            <input v-model="repassword" type="password" class="form-control" />
        </div>
        <button class="btn btn-secondary" @click="onRegister">注册</button>
    </div>
</template>
  
<script>

export default {
    components: {
    },
    data() {
        return {
            username: '',
            password: '',
            repassword: '',
        };
    },
    methods: {
        onRegister() {
            // 处理注册逻辑
            // 如果登录成功，发射 login 事件
            this.$emit('signup');
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