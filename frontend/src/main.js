import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css'
import '../icon/font_h0buycu09np/iconfont.css'
import '../icon/font_p53cqr1958d/iconfont-custom.css'
import '../icon/font_kx3mi79d359/iconfont-custom.css'
import '../icon/font_hnwf06o65kh/iconfont-custom.css'
import '../icon/font_77vsboeqc5p/iconfont-custom.css'

createApp(App).use(router).mount('#app')

// 移除加载提示
const el = document.getElementById('app-loading')
if (el) el.remove()