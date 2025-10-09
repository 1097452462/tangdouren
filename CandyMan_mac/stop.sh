#!/bin/bash

echo "正在强制关闭所有相关进程..."
echo ""

# 终止服务端进程
echo "关闭服务器进程..."
pkill -9 -f "SugarBeanServer" >/dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✓ 服务器进程已关闭"
else
    echo "⚠️ 未找到服务器进程"
fi

# 终止客户端进程
echo "关闭客户端进程..."
pkill -9 -f "SugarBean" >/dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✓ 客户端进程已关闭"
else
    echo "⚠️ 未找到客户端进程"
fi

echo ""
echo "所有操作执行完成！"