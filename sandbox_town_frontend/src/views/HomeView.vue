<template>
  <div class="container">
    <div class="d-flex justify-content-center mt-5">
      <button class="btn" @click="showLoginForm" buttonClass="me-2">登录</button>
      <button class="btn" @click="showRegisterForm" buttonClass="me-2">注册</button>
      <button class="btn" buttonClass="me-2"><a style="text-decoration:none; color:inherit;"
          href="https://github.com/shui-dun/SandboxTown" target="_blank">
          关于
        </a></button>
    </div>
    <div v-if="isLoginFormVisible">
      <login-form></login-form>
    </div>
    <div v-if="isRegisterFormVisible">
      <register-form></register-form>
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
  
<style></style>
  