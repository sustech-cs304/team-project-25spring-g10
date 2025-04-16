#!/usr/bin/env python
"""
API测试命令行脚本
用于直接运行API连接测试
"""

import sys
import os

# 确保当前目录在Python路径中
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
if parent_dir not in sys.path:
    sys.path.insert(0, parent_dir)

# 导入测试模块
from mcp_server.tests.test_api_connection import test_all

if __name__ == "__main__":
    print("运行API连接测试...")
    test_all() 