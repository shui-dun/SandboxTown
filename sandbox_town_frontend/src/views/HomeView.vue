<template>
  <div class="simple-bg" v-if="isMobile">
    <div class="simple-prompt">
      <h1>沙盒小镇 🏠</h1>
      <p class="simple-p">使用电脑或平板等大屏设备访问</p>
      <button class="btn btn-secondary" buttonClass="me-2"><a style="text-decoration:none; color:inherit;"
          href="https://github.com/shui-dun/SandboxTown" target="_blank">
          关于
        </a></button>
    </div>
  </div>
  <div class="simple-bg" v-else-if="isVertical">
    <div class="simple-prompt">
      <h1>沙盒小镇 🏠</h1>
      <p class="simple-p">请切换到横屏模式</p>
      <button class="btn btn-secondary" buttonClass="me-2"><a style="text-decoration:none; color:inherit;"
          href="https://github.com/shui-dun/SandboxTown" target="_blank">
          关于
        </a></button>
    </div>
  </div>
  <div v-else>
    <CircleBackground />
    <div id="home-page-bg">
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
  </div>
</template>
  
  
<script>
import LoginForm from '../components/LoginForm.vue';
import RegisterForm from '../components/RegisterForm.vue';
import CircleBackground from '@/components/CircleBackground.vue';

export default {
  components: {
    LoginForm,
    RegisterForm,
    CircleBackground,
  },
  mounted() {
    let myInterval = setInterval(() => {
      if (!this.checkIsVertical()) {
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
      isVertical: this.checkIsVertical(),
      isMobile: this.checkIsMobile(),
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
    checkIsVertical() {
      return window.innerWidth < window.innerHeight;
    },
    checkIsMobile() {
      return window.innerWidth < 500 || window.innerHeight < 500;
    }
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
</style>
  