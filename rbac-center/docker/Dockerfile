# 基础镜像
FROM adoptopenjdk/openjdk8:jdk8u382-b05-alpine

# 将打包好的项目添加到镜像中
WORKDIR /app
ADD . ./

# 添加脚本的执行权限
RUN chmod +x ./start.sh

# 设置项目启动时脚本
ENTRYPOINT ["sh","-c","./start.sh"]