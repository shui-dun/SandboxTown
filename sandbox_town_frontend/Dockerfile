# 使用官方的 node 镜像作为基础镜像
FROM node:18-alpine

# 设置工作目录
WORKDIR /app

# 将 package.json 和 package-lock.json 复制到工作目录
# 之所以不在一开始就复制所有文件，是为了利用 docker 缓存机制
# 当除开 package.json 和 package-lock.json 之外的其他文件发生变化时，不会重新执行 npm install
COPY package*.json ./

# 安装项目依赖
RUN npm install

# 复制项目文件到工作目录
COPY . .

# 构建项目
RUN npm run build

# 使用 nginx 镜像作为基础镜像
# 使用了多阶段构建：第一阶段构建项目，第二阶段使用 nginx 镜像作为基础镜像
# 这样可以减少最终镜像的大小，因为第二阶段构建的镜像中不包含 node_modules
FROM nginx:1.25-alpine

# 将构建的文件从上一阶段复制到 nginx 的默认目录
# --from=0 表示从第一个阶段（索引为0）复制文件
COPY --from=0 /app/dist /usr/share/nginx/html

# 将 nginx.conf 复制到 nginx 的默认目录
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露 80 端口
EXPOSE 80
# 暴露 443 端口
EXPOSE 443

# 启动 nginx
# -g 是 nginx 的一个命令行参数，用于定义全局指令
# daemon off 表示以前台进程的方式启动 nginx
# 这在容器中是必要的，因为容器的主进程（在这种情况下是 nginx）应该在前台运行
CMD ["nginx", "-g", "daemon off;"]

