<template>
    <ProcessBar v-if="showProcessBar" :duration="processBarDuration" :text="processBarText"
        @progressComplete="onProgressComplete" />
</template>
<script>
import ProcessBar from './ProcessBar.vue';
import emitter from '@/js/mitt';
import mixin from '@/js/mixin';

export default {
    components: {
        ProcessBar,
    },
    data() {
        return {
            // 进度条相关设置
            showProcessBar: false,
            processBarDuration: 3,
            processBarText: '正在摘苹果...',
            // 摘苹果的人
            initator: '',
            // 摘苹果的树
            target: '',
        }
    },
    methods: {
        onProgressComplete() {
            this.showProcessBar = false;
            // 向后端发送摘苹果请求
            mixin.myPOST('/rest/tree/pickApple',
                new URLSearchParams({
                    treeId: this.target,
                }),
                () => {
                    mixin.fadeInfoShow('获得苹果');
                },
            );
        },
    },
    mounted() {
        emitter.on('TREE_ARRIVE', (data) => {
            this.target = data.target;
            this.initator = data.initator;
            // 首先询问后端，检查是否可以摘苹果
            mixin.myGET('/rest/tree/canPickApple',
                new URLSearchParams({
                    treeId: this.target,
                }),
                () => {
                    // 如果可以摘苹果
                    this.showProcessBar = true;
                }
            );
        });
        emitter.on('MOVE', (data) => {
            // 终止进度条
            if (data.id === this.initator) {
                this.showProcessBar = false;
            }
        });
    },
};

</script>