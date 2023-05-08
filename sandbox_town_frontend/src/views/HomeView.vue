<template>
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
</template>
  
  
<script>
import LoginForm from '../components/LoginForm.vue';
import RegisterForm from '../components/RegisterForm.vue';

export default {
  components: {
    LoginForm,
    RegisterForm,
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
  
<style>
#home-page-bg {
  width: 100%;
  height: 100%;
  background: url("../assets/home-page-bg.png") center center no-repeat;
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
</style>
  