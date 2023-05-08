<template>
  <div id="app">
    <div class="buttons">
      <ButtonComponent @click="showSignIn">登录</ButtonComponent>
      <ButtonComponent @click="showSignUp">注册</ButtonComponent>
    </div>
    <SignIn v-if="isSignInVisible" />
    <SignUp v-if="isSignUpVisible" />
  </div>
</template>

<script>
import ButtonComponent from './components/ButtonComponent.vue';
import SignIn from './components/SignIn.vue';
import SignUp from './components/SignUp.vue';

export default {
  components: {
    ButtonComponent,
    SignIn,
    SignUp,
  },
  data() {
    return {
      isSignInVisible: true,
      isSignUpVisible: false,
    };
  },
  methods: {
    showSignIn() {
      this.isSignInVisible = true;
      this.isSignUpVisible = false;
    },
    showSignUp() {
      this.isSignInVisible = false;
      this.isSignUpVisible = true;
    },
  },
};
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}

.buttons {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

ButtonComponent {
  margin-right: 1rem;
}

ButtonComponent:last-child {
  margin-right: 0;
}
</style>