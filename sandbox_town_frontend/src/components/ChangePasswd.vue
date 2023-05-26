<template>
    <div>
        <div class="mb-3">
            <label class="form-label">用户名</label>
            <input value="player1" type="text" class="form-control" readonly />
        </div>
        <div class="mb-3">
            <label class="form-label">原密码</label>
            <input v-model="oldpassword" type="password" class="form-control" />
        </div>
        <div class="mb-3">
            <label class="form-label">新密码</label>
            <input v-model="newpassword" type="password" class="form-control" />
        </div>
        <div class="mb-3">
            <label class="form-label">再次输入新密码</label>
            <input v-model="repassword" type="password" class="form-control" />
        </div>
        <button class="btn btn-secondary" @click="onChangePasswd">修改密码</button>
    </div>
</template>
  
<script>

export default {
    components: {
    },
    data() {
        return {
            oldpassword: '',
            newpassword: '',
            repassword: '',
        };
    },
    methods: {
        onChangePasswd() {
            // 处理修改密码逻辑
            // 如果登录成功，发射事件
            this.$emit('info', '修改密码成功');
        },
    },
};
</script>
