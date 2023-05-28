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
    mounted() {
        // 获取用户名后缀
        fetch('/rest/user/getUsername', {
            method: 'GET',
        }).then(response => response.json())
            .then(data => {
                if (data.code === 0) {
                    this.usernameSuffix = data.data.slice(5);
                } else {
                    this.fadeInfoShow(data.msg);
                }
            }).catch(error => {
                this.fadeInfoShow(`请求出错: ${error}`);
            });
    },
    methods: {
        onChangePasswd() {
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
            fetch('/rest/user/changePassword', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams({
                    oldPassword: this.oldpassword,
                    newPassword: this.newpassword,
                }),
            }).then(response => response.json())
                .then(data => {
                    if (data.code === 0) {
                        this.fadeInfoShow('修改密码成功');
                    } else {
                        this.fadeInfoShow(data.msg);
                    }
                }).catch(error => {
                    this.fadeInfoShow(`请求出错: ${error}`);
                });
        },
    },
};
</script>
