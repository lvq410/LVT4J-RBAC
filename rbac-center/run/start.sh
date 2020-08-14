#!/bin/sh
#切换至工作目录
shellDir=`dirname $0`
cd $shellDir/..
#检查重复启动
if [ -f "./applicationPid" ]; then
    ePid=`cat ./applicationPid`
    echo '检测到服务已运行于进程'$ePid'上,结束启动命令!'
    exit 0
fi
#用gradle打出jar包rbac-center.jar
gradle clean
gradle bootRepackage
mv ./build/libs/rbac-center.jar ./rbac-center.jar
#配置java命令及参数
JAVA_EXE=$JAVA_HOME/bin/java
args="-Dfile.encoding=utf-8
    -Xmx3210m
    -Xmn1024m
    -XX:+UseParNewGC
    -XX:+UseConcMarkSweepGC
    -XX:CMSInitiatingOccupancyFraction=80
    -XX:+PrintGCDetails
    -XX:+PrintGCTimeStamps
    -XX:+PrintHeapAtGC
    -Xloggc:logs/gc.log
    -XX:PermSize=64M
    -XX:MaxNewSize=1256m
    -XX:MaxPermSize=128m"
$JAVA_EXE $args -jar rbac-center.jar >/dev/null 2>&1 &
#等待5秒以保证应用进程id文件创建出来
sleep 5
if [ -f "./applicationPid" ]; then
    pid=`cat ./applicationPid`
    echo '启动成功!服务运行于进程'$pid
else
    echo '服务启动失败!'
    echo '进程文件./applicationPid不存在!'
fi