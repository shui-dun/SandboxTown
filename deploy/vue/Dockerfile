# 使用官方的 node 镜像作为基础镜像
FROM node:14

# 设置工作目录
WORKDIR /app

# 将 package.json 和 package-lock.json 复制到工作目录
COPY package*.json ./

# 安装项目依赖
RUN npm install

# 复制项目文件到工作目录
COPY . .

# 构建项目
RUN npm run build

# 使用 nginx 镜像作为基础镜像
FROM nginx:alpine

# 将构建的文件从上一阶段复制到 nginx 的默认目录
COPY --from=0 /app/dist /usr/share/nginx/html

# 暴露 80 端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]

