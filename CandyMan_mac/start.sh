#!/bin/bash

# 运行server（需确保是macOS可执行文件）
./SugarBeanServer &

# 等待1秒
sleep 1

# 运行客户端程序
open ./client/SugarBean.app