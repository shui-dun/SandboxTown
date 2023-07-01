<template>
    <router-view />
    <FadeInfo ref="fadeInfo" />
</template>
<script>
import FadeInfo from '@/components/FadeInfo.vue';
import mixin from "@/js/mixin.js";

export default {
    components: {
        FadeInfo,
    },
    mounted() {
        // 设置全局的工具类里面的fadeInfoShow方法
        mixin.fadeInfoShow = (msg) => {
            this.$refs.fadeInfo.showInfo(msg)
        };
    },
    methods: {
    },
};
</script>
<style>
@import '~bootstrap/dist/css/bootstrap.min.css';
</style>