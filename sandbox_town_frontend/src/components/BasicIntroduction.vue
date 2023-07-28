<template>
    <div class="basic-introduction">
        <div>
            <div class="basic-intro-header">
                <!-- 物品名称 -->
                <p>{{ name }}</p>
            </div>
            <div class="basic-intro-content">{{ description }}</div>
        </div>
        <div>
            <img width="150" :src="image" />
        </div>
    </div>
</template>
<script>
export default {
    props: {
        name: {
            type: String,
            required: true,
        },
        description: {
            type: String,
            required: true,
        },
        image: {
            type: String,
            required: true,
        },
    },
};
</script>
<style scoped>
.basic-introduction {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    padding: 20px;
    margin-bottom: 20px;
    border-radius: 10px;
    background-color: #fff;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.basic-intro-header {
    font-size: 28px;
    font-weight: bold;
    color: #333;
}

.basic-intro-content {
    font-size: 18px;
    color: #333;
    margin-bottom: 20px;
    min-width: 200px;
}

</style>