<template>
    <router-view />
    <FadeInfo ref="fadeInfo" />
</template>
<script>
import FadeInfo from '@/components/FadeInfo.vue';
import myUtils from "@/js/myUtils.js";

export default {
    components: {
        FadeInfo,
    },
    mounted() {
        // 设置全局的工具类里面的fadeInfoShow方法
        myUtils.fadeInfoShow = (msg) => {
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