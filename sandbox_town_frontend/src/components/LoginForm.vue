
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
        <div class="mb-3 form-check">
            <input v-model="rememberMe" type="checkbox" class="form-check-input" />
            <label class="form-check-label">记住密码</label>
        </div>
        <button class="btn btn-secondary" @click="onLogin">登录</button>
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
            rememberMe: true, // 默认记住密码
        };
    },
    methods: {
        onLogin() {
            if (this.username === '') {
                mixin.fadeInfoShow('用户名不能为空');
                return;
            }
            if (this.password === '') {
                mixin.fadeInfoShow('密码不能为空');
                return;
            }
            mixin.myPOSTUrlEncoded(
                '/rest/user/login',
                {
                    username: 'USER_' + this.username,
                    password: this.password,
                    rememberMe: this.rememberMe,
                },
                () => {
                    mixin.fadeInfoShow('登录成功');
                    this.$emit('login');
                },
            );
            
        },
    },
};
</script>
