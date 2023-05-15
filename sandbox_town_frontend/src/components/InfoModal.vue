<template>
    <div v-if="show" class="modal" tabindex="-1" style="display: block; background-color: rgba(0, 0, 0, 0.5);">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-body">
                    <p>{{ message }}</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" @click="close">OK</button>
                </div>
            </div>
        </div>
    </div>
</template>
  
<script>
export default {
    data() {
        return {
            message: '',
            show: false,
        };
    },
    methods: {
        close() {
            this.show = false;
        },
        showInfo(msg) {
            this.message = msg;
            this.show = true;
        },
    },
};
</script>
  