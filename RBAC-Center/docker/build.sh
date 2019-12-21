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
echo "开始构建rbac-center:latest镜像,项目路径："$shellDir
#用gradle打出jar
gradle clean
gradle bootRepackage
#整理打镜像用文件
mkdir ./build/docker
mv ./build/libs/*.jar ./build/docker/app.jar
cp ./docker/Dockerfile ./build/docker/
cp ./docker/start.sh ./build/docker/start.sh
sed -i 's/^M//g' ./build/docker/start.sh
cp -r ./web ./build/docker/
cd ./build/docker
#打镜像
docker build -t lvq410/rbac:latest . 
#推镜像
docker push lvq410/rbac:latest