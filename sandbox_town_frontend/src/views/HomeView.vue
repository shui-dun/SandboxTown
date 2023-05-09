<template>
  <div v-if="!isMobile" id="home-page-bg">
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
  <div id="mobile-bg" v-else>
    <div id="mobile-prompt">
      <h1 id="mobile-h">沙盒小镇 🏠</h1>
      <p>请使用电脑或平板等大屏幕设备访问</p>
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
}

#home-page {
  padding-bottom: 30px;
  padding-top: 10px;

  width: 30%;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 30px;
}

#mobile-bg {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  position: fixed;
}

#mobile-prompt {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(240, 240, 240, 0.7);
  border-radius: 10px;
  padding: 20px;

}

#mobile-h {
  margin-bottom: 30px;
  text-align: center;
}
</style>
  