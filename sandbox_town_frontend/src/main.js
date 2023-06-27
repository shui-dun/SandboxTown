import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

let app = createApp(App)
app.use(router)
// app.config.errorHandler = (err, vm, info) => {
//     console.error(err)
// }
app.mount('#app')
