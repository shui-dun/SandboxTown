<template>
    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="myModalLabel">Hello</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body text-center">
            <div class="input-group mb-3">
              <button class="btn btn-outline-secondary" type="button" @click="decrement">-</button>
              <input type="text" class="form-control" v-model="count" readonly>
              <button class="btn btn-outline-secondary" type="button" @click="increment">+</button>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" @click="confirm">确定</button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    data() {
      return {
        count: 0
      };
    },
    methods: {
      increment() {
        this.count++;
      },
      decrement() {
        this.count--;
      },
      confirm() {
        // 处理确定按钮的逻辑
      }
    }
  };
  </script>
  
  <style>
  /* 在这里添加样式以美化弹出窗口 */
  </style>