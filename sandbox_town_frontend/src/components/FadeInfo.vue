<template>
    <div class="fade-info-wrapper">
        <transition name="fade" v-for="(itemVal, itemKey) in messages" :key="itemKey">
            <div v-if="show[itemKey]" class="fade-info-alert">
                <div class="fade-info-edge"></div>
                <div class="alert-content">{{ itemVal }}</div>
                <div class="fade-info-edge"></div>
            </div>
        </transition>
    </div>
</template>
  
<script>

import emitter from "@/js/mitt";
const { v4: uuidv4 } = require('uuid');

export default {
    data() {
        return {
            messages: {},
            show: {}
        };
    },
    mounted() {
        emitter.on("SPRITE_ATTRIBUTE_CHANGE", msg => {
            let showMap = {
                "moneyInc": "💰 金钱",
                "expInc": "📖 经验",
                "levelInc": "📈 等级",
                "hungerInc": "🍔 饥饿",
                "hpInc": "❤️ 血量",
                "attackInc": "⚔️ 攻击力",
                "defenseInc": "🛡️ 防御力",
                "speedInc": "🏃 速度",
            };
            let showMsg = (attr) => {
                if (msg[attr] > 0) {
                    this.showInfo(`您的${showMap[attr]}增加了${msg[attr]}`);
                } else if (msg[attr] < 0) {
                    this.showInfo(`您的${showMap[attr]}减少了${-msg[attr]}`);
                }
            }
            for (let key in showMap) {
                // 如果等级提升了，那么不显示经验值的变化
                if (key === "expInc" && msg["levelInc"] > 0) {
                    continue;
                }
                showMsg(key);
            }
        });
    },
    methods: {
        showInfo(msg) {
            const uuid = uuidv4();
            this.messages[uuid] = msg;
            this.show[uuid] = true;
            setTimeout(() => {
                this.show[uuid] = false;
                setTimeout(() => {
                    delete this.messages[uuid];
                    delete this.show[uuid];
                }, 800);
            }, 2500);
        },
    },
};
</script>
  
<style scoped>
.fade-info-wrapper {
    display: flex;
    position: fixed;
    bottom: 2%;
    left: 50%;
    flex-direction: column;
    align-items: flex-start;
    /* 使子元素根据其内容自适应宽度，而不是填满容器 */
    z-index: 300;
}

.fade-info-alert {
    /* display: flex; */
    flex-grow: 0;
    margin: 5px;
    transform: translateX(-50%);
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    border-radius: 5px;
    padding: 10px;
}

.fade-info-edge {
    width: 10px;
    height: 100%;
    background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 10 100" preserveAspectRatio="none"><path d="M0,0 Q5,50 10,0 Q5,50 0,100" fill="%23f5c6cb"/></svg>');
}

.alert-content {
    padding: 0 10px;
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.5s;
}

.fade-enter,
.fade-leave-to {
    opacity: 0;
}
</style>
  