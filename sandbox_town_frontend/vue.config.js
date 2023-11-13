const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  chainWebpack: config => {
    config.module
      .rule('images')
      .set('parser', {
        dataUrlCondition: {
          // 禁用base64编码图片，因为phaser.js中的base64编码图片会导致无法加载
          maxSize: -1
        }
      })
  },
  transpileDependencies: true,
  devServer: {
    proxy: {
      '/rest': {
        target: 'http://sandboxtown-back:9090',
        changeOrigin: true,
        pathRewrite: {
          '^/rest': '', // 删除 /rest 前缀
        },
      },
      '/websocket': {
        target: 'http://sandboxtown-back:9090',
        ws: true,
        changeOrigin: true,
        pathRewrite: {
        },
      }
    }
  },
})


