<template>
  <router-view />
</template>
<script>
export default {
  mounted() {
    this.$router.push('/');
  }
}
</script>
<style>
@import '~bootstrap/dist/css/bootstrap.min.css';
</style>