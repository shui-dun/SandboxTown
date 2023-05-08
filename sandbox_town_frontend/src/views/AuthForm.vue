<template>
    <div>
      <nav class="navbar navbar-expand-lg navbar-dark bg-info">
        <div class="container-fluid">
          <a class="navbar-brand" href="#">My App</a>
          <button
            class="navbar-toggler"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#navbarNav"
            aria-controls="navbarNav"
            aria-expanded="false"
            aria-label="Toggle navigation"
          >
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav ms-auto">
              <li class="nav-item">
                <a class="nav-link" href="#" @click="showLoginForm">登录</a>
              </li>
              <li class="nav-item">
                <a class="nav-link" href="#" @click="showRegisterForm">注册</a>
              </li>
            </ul>
          </div>
        </div>
      </nav>
  
      <div class="container">
        <div class="row">
          <div class="col-md-6 mx-auto mt-5">
            <div class="card" v-if="isLoginFormVisible">
              <div class="card-header">登录</div>
              <div class="card-body">
                <form @submit.prevent="login">
                  <div class="mb-3">
                    <label for="login-username" class="form-label">邮箱或用户名</label>
                    <input type="text" class="form-control" id="login-username" v-model="loginUsername" required />
                  </div>
                  <div class="mb-3">
                    <label for="login-password" class="form-label">密码</label>
                    <input type="password" class="form-control" id="login-password" v-model="loginPassword" required />
                  </div>
                  <button type="submit" class="btn btn-primary">登录</button>
                </form>
              </div>
            </div>
  
            <div class="card" v-if="isRegisterFormVisible">
              <div class="card-header">注册</div>
              <div class="card-body">
                <form @submit.prevent="register">
                  <div class="mb-3">
                    <label for="register-email" class="form-label">邮箱</label>
                    <input type="email" class="form-control" id="register-email" v-model="registerEmail" required />
                  </div>
                  <div class="mb-3">
                    <label for="register-username" class="form-label">用户名</label>
                    <input type="text" class="form-control" id="register-username" v-model="registerUsername" required />
                  </div>
                  <div class="mb-3">
                    <label for="register-password" class="form-label">密码</label>
                    <input type="password" class="form-control" id="register-password" v-model="registerPassword" required />
                  </div>
                  <button type="submit" class="btn btn-primary">注册</button>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    data() {
      return {
        isLoginFormVisible: true,
        isRegisterFormVisible: false,
        loginUsername: '',
        loginPassword: '',
        registerEmail: '',
        registerUsername: '',
        registerPassword: '',
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
      login() {
        // 在这里处理登录逻辑
        console.log('登录:', this.loginUsername, this.loginPassword);
      },
      register() {
        // 在这里处理注册逻辑
        console.log('注册:', this.registerEmail, this.registerUsername, this.registerPassword);
      },
    },
  };
  </script>
  