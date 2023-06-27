<template>
    <div class="floating-button">
    </div>
</template>
<script>
export default {
    methods: {
    },
};
</script>
<style scoped>
.floating-button {
    background-image: url('../assets/img/BACKPACK.jpg');
    background-size: 100% 100%;
    /* 固定位置 */
    position: fixed;
    bottom: 20px;
    right: 20px;
    width: 50px;
    /* 可根据需要调整大小 */
    height: 50px;
    border-radius: 50%;
    /* border-width: 50px;
    border-color: #25b0f3; */
    /* 可根据需要调整背景颜色 */
    /* display: flex; */
    /* justify-content: center; */
    /* align-items: center; */
    cursor: pointer;
    z-index: 100;
    /* 确保按钮位于页面的最上层 */
}
</style>
  