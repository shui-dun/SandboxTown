<template>
    <div>
        <div class="mb-3">
            <label class="form-label">用户名</label>
            <input :value="usernameSuffix" type="text" class="form-control" readonly />
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
import myUtils from "@/js/myUtils.js";

export default {
    inject: ['fadeInfoShow'],
    components: {
    },
    data() {
        return {
            oldpassword: '',
            newpassword: '',
            repassword: '',
            usernameSuffix: '',
        };
    },
    async mounted() {
        // 获取用户名后缀
        this.usernameSuffix = (await myUtils.myGET('/rest/user/getUsername', null)).slice(5);
    },
    methods: {
        async onChangePasswd() {
            // 检查旧密码是否为空
            if (this.oldpassword === '') {
                this.fadeInfoShow('旧密码不能为空');
                return;
            }
            // 新密码长度不能小于 6 位
            if (this.newpassword.length < 6) {
                this.fadeInfoShow('新密码长度不能小于 6 位');
                return;
            }
            // 检查两次输入的密码是否一致
            if (this.newpassword !== this.repassword) {
                this.fadeInfoShow('两次输入的密码不一致');
                return;
            }
            // 向后端发送修改密码请求
            await myUtils.myPOST(
                '/rest/user/changePassword',
                new URLSearchParams({
                    oldPassword: this.oldpassword,
                    newPassword: this.newpassword,
                }),
                () => {
                    this.fadeInfoShow('修改密码成功');
                },
            );
        },
    },
};
</script>
