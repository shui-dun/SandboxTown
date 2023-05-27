<template>
    <!-- 如果是移动端，显示提示信息 -->
    <div class="simple-bg" v-if="isMobile">
        <div class="simple-prompt">
            <h1>沙盒小镇 🏠</h1>
            <p class="simple-p">使用电脑或平板等大屏设备访问</p>
            <button class="btn btn-secondary"><a style="text-decoration:none; color:inherit;"
                    href="https://github.com/shui-dun/SandboxTown" target="_blank">
                    关于
                </a></button>
        </div>
    </div>
    <!-- 如果是竖屏，显示提示信息 -->
    <div class="simple-bg" v-else-if="isVertical">
        <div class="simple-prompt">
            <h1>沙盒小镇 🏠</h1>
            <p class="simple-p">请切换到横屏模式</p>
            <button class="btn btn-secondary"><a style="text-decoration:none; color:inherit;"
                    href="https://github.com/shui-dun/SandboxTown" target="_blank">
                    关于
                </a></button>
        </div>
    </div>
    <!-- 否则，显示主面板 -->
    <div v-else>
        <CircleBackground />
        <div id="home-page-bg">
            <div class="container" id="home-page">
                <div class="d-flex justify-content-center mb-3">
                    <button class="btn" v-if="!isLogin" @click="curTab = 'login'"
                        :class="{ active: curTab == 'login' }">登录</button>
                    <button class="btn" v-if="!isLogin" @click="curTab = 'signup'"
                        :class="{ active: curTab == 'signup' }">注册</button>
                    <button class="btn" v-if="isLogin" @click="curTab = 'mapChoose'"
                        :class="{ active: curTab == 'mapChoose' }">选择地图</button>
                    <button class="btn" v-if="isLogin" @click="doLogout">退出登录</button>
                    <button class="btn" v-if="isLogin" @click="curTab = 'changePasswd'"
                        :class="{ active: curTab == 'changePasswd' }">修改密码</button>
                    <button class="btn"><a style="text-decoration:none; color:inherit;"
                            href="https://github.com/shui-dun/SandboxTown" target="_blank">
                            关于
                        </a>
                    </button>
                </div>
                <div v-if="curTab == 'login'" class="form myform">
                    <LoginForm @login="isLogin = true; curTab = 'mapChoose'" />
                </div>
                <div v-else-if="curTab == 'signup'" class="form myform">
                    <RegisterForm @signup="isLogin = true; curTab = 'mapChoose'" />
                </div>
                <div v-else-if="curTab == 'mapChoose'" class="form myform">
                    <MapChoose />
                </div>
                <div v-else-if="curTab == 'changePasswd'" class="form myform">
                    <ChangePasswd />
                </div>
            </div>
        </div>
        <FadeInfo ref="fadeInfo" />
    </div>
</template>
    
    
<script>
import LoginForm from '../components/LoginForm.vue';
import RegisterForm from '../components/RegisterForm.vue';
import CircleBackground from '@/components/CircleBackground.vue';
import MapChoose from '@/components/MapChoose.vue';
import ChangePasswd from '@/components/ChangePasswd.vue';
import FadeInfo from '@/components/FadeInfo.vue';

export default {
    provide() {
        return {
            fadeInfoShow: this.fadeInfoShow,
        };
    },
    components: {
        LoginForm,
        RegisterForm,
        CircleBackground,
        MapChoose,
        ChangePasswd,
        FadeInfo,
    },
    mounted() {
        // 判断是否是横屏
        let myInterval = setInterval(() => {
            if (!this.checkIsVertical()) {
                // 当前设备是横屏
                this.isVertical = false;
                clearInterval(myInterval);
            }
        }, 1000);
        // 检查是否登录
        this.checkIsLogin();
    },
    data() {
        return {
            isVertical: this.checkIsVertical(),
            isMobile: this.checkIsMobile(),
            isLogin: false,
            curTab: 'login',
        };
    },
    methods: {
        checkIsVertical() {
            return window.innerWidth < window.innerHeight;
        },
        checkIsMobile() {
            return window.innerWidth < 500 || window.innerHeight < 500;
        },
        checkIsLogin() {
            // 向后端发送请求，检查是否登录
            fetch('/rest/user/islogin', {
                method: 'GET',
            }).then((response) => response.json())
                .then((data) => {
                    if (data.code == 0) {
                        if (data.data == true) {
                            this.isLogin = true;
                            this.curTab = 'mapChoose';
                        } else {
                            this.isLogin = false;
                            this.curTab = 'login';
                        }
                    } else {
                        this.fadeInfoShow(data.msg);
                    }
                })
                .catch(error => {
                    this.fadeInfoShow(`请求出错: ${error}`);
                });
        },
        doLogout() {
            // 向后端发送请求，退出登录
            fetch('/rest/user/logout', {
                method: 'POST',
            }).then((response) => response.json())
                .then((data) => {
                    if (data.code == 0) {
                        this.isLogin = false;
                        this.curTab = 'login';
                    } else {
                        this.fadeInfoShow(data.msg);
                    }
                })
                .catch(error => {
                    this.fadeInfoShow(`请求出错: ${error}`);
                });
        },
        fadeInfoShow(msg) {
            this.$refs.fadeInfo.showInfo(msg);
        },
    },
};
</script>
    
<style scoped>
#home-page-bg {
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    background-size: 100% 100%;
    position: fixed;
    z-index: 100;
    display: flex;
    align-items: center;
    justify-content: center;
}

#home-page {
    padding-bottom: 30px;
    padding-top: 10px;

    width: 400px;
    background: rgba(240, 240, 240, 0.8);
    border-radius: 30px;
}

.simple-bg {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    position: fixed;
}

.simple-prompt {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background: rgba(240, 240, 240, 0.7);
    border-radius: 10px;
    padding: 20px;

}

.simple-p {
    margin-top: 15px;
    margin-bottom: 20px;
    text-align: center;
}

.myform {
    width: 80%;
    margin: 0 auto;
}

.active {
    background: rgba(220, 220, 220, 0.8);
}
</style>
    