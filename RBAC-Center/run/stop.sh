#!/bin/sh
shellDir=`dirname $0`
cd $shellDir/..
if [ ! -f "./applicationPid" ]; then
    echo '服务早已停止!'
else
    pid=`cat ./applicationPid`
    kill -9 $pid
    rm ./applicationPid
    echo '已关停服务运行的进程'$pid
fi