<template>
  <div class="simple-bg" v-if="isMobile">
    <div class="simple-prompt">
      <h1>沙盒小镇 🏠</h1>
      <p class="simple-p">请使用电脑或平板等大屏幕设备访问</p>
      <button class="btn btn-secondary" buttonClass="me-2"><a style="text-decoration:none; color:inherit;"
          href="https://github.com/shui-dun/SandboxTown" target="_blank">
          关于
        </a></button>
    </div>
  </div>
  <div class="simple-bg" v-else-if="isVertical">
    <div class="simple-prompt">
      <h1>沙盒小镇 🏠</h1>
      <p class="simple-p">请切换到横屏模式后刷新重新访问</p>
      <button class="btn btn-secondary" buttonClass="me-2"><a style="text-decoration:none; color:inherit;"
          href="https://github.com/shui-dun/SandboxTown" target="_blank">
          关于
        </a></button>
    </div>
  </div>
  <div v-else id="home-page-bg">
    <div class="container" id="home-page">
      <div class="d-flex justify-content-center">
        <button class="btn" @click="showLoginForm" buttonClass="me-2">登录</button>
        <button class="btn" @click="showRegisterForm" buttonClass="me-2">注册</button>
        <button class="btn" buttonClass="me-2"><a style="text-decoration:none; color:inherit;"
            href="https://github.com/shui-dun/SandboxTown" target="_blank">
            关于
          </a></button>
      </div>
      <div v-if="isLoginFormVisible" class="form">
        <login-form></login-form>
      </div>
      <div v-if="isRegisterFormVisible" class="form">
        <register-form></register-form>
      </div>
    </div>
  </div>
</template>
  
  
<script>
import LoginForm from '../components/LoginForm.vue';
import RegisterForm from '../components/RegisterForm.vue';

export default {
  components: {
    LoginForm,
    RegisterForm,
  },
  created() {
    if (window.screen.width < 500) {
      // 当前设备是移动设备
      this.isMobile = true;
    } else {
      this.isMobile = false;
    }
    if (window.screen.width < window.screen.height) {
      // 当前设备是竖屏
      this.isVertical = true;
    } else {
      this.isVertical = false;
    }
  },
  data() {
    return {
      isLoginFormVisible: true,
      isRegisterFormVisible: false,
    };
  },
  methods: {
    showLoginForm() {
      this.isLoginFormVisible = true;
      this.isRegisterFormVisible = false;
    },
    showRegisterForm() {
      this.isLoginFormVisible = false;
      this.isRegisterFormVisible = true;
    },
  },
};
</script>
  
<style scoped>
#home-page-bg {
  width: 100%;
  height: 100%;
  background: url("../assets/img/home-page-bg.png") center center no-repeat;
  background-size: 100% 100%;
  position: fixed;

  display: flex;
  align-items: center;
  justify-content: center;
}

#home-page {
  padding-bottom: 30px;
  padding-top: 10px;

  width: 400px;
  background: rgba(255, 255, 255, 0.7);
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
}
</style>
  