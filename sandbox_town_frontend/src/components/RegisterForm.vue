<template>
    <div>
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
import mixin from "@/js/mixin.js";

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
            // 检查用户名是否过短
            if (this.username.length < 3) {
                mixin.fadeInfoShow('用户名过短');
                return;
            }
            // 密码长度不能小于 6 位
            if (this.password.length < 6) {
                mixin.fadeInfoShow('密码长度不能小于 6 位');
                return;
            }
            // 检查两次输入的密码是否一致
            if (this.password !== this.repassword) {
                mixin.fadeInfoShow('两次输入的密码不一致');
                return;
            }
            // 向后端发送注册请求
            mixin.myPOSTUrlEncoded(
                '/rest/user/signup',
                {
                    usernameSuffix: this.username,
                    password: this.password,
                },
                () => {
                    mixin.fadeInfoShow('注册成功');
                    this.$emit('signup');
                },
            );
        },
    },
};
</script>
  
<style scoped>
</style>