<template>
  <div class="simple-bg" v-if="isVertical">
    <div class="simple-prompt">
      <h1>沙盒小镇 🏠</h1>
      <p class="simple-p">请切换到横屏模式后重新访问<br>推荐使用电脑和平板等大屏设备</p>
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
  mounted() {
    let myInterval = setInterval(() => {
      if (window.innerWidth > window.innerHeight) {
        // 当前设备是横屏
        this.isVertical = false;
        clearInterval(myInterval);
      }
    }, 1000);
  },
  data() {
    return {
      isLoginFormVisible: true,
      isRegisterFormVisible: false,
      isVertical: window.innerWidth < window.innerHeight,
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
  background: rgba(255, 255, 255, 0.8);
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
</style>
  