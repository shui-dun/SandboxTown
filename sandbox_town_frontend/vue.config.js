const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    proxy: {
      '/rest': {
        target: 'http://localhost:9090',
        // ws: true,
        changeOrigin: true,
        pathRewrite: {
          '^/rest': '', // 删除 /rest 前缀
        },
      }
    }
  }
})


