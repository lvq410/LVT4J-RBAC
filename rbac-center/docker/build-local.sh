#!/bin/sh
#############################
#docker镜像构建脚本
#本地构建要求有docker环境，默认jdk18，默认gradle5.0+
#############################
set -e
#切换至工作目录
shellDir=`dirname $0`
cd $shellDir/..
shellDir=`pwd`
tag=latest
echo "开始构建rbac:$tag镜像,项目路径："$shellDir
#用gradle打出jar
export GRADLE_HOME=/d/gradle/gradle-2.12
alias gradle=$GRADLE_HOME/bin/gradle;
gradle clean
gradle bootRepackage
#整理打镜像用文件
mkdir ./build/docker
mv ./build/libs/*.jar ./build/docker/app.jar
sed 's/^M//g' ./docker/Dockerfile > ./build/docker/Dockerfile
sed 's/^M//g' ./docker/start.sh > ./build/docker/start.sh
cp -r ./web ./build/docker/
cd ./build/docker

#打镜像
docker build -t harbor-registry.inner.youdao.com/course/rbac:$tag . 
#推镜像
docker push harbor-registry.inner.youdao.com/course/rbac:$tag
#打镜像
docker build -t lvq410/rbac:$tag . 
#推镜像
docker push lvq410/rbac:$tag