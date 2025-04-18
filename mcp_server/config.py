"""
配置文件，存储应用程序的各种配置项
"""
import os

# API基础URL
BASE_URL = os.environ.get("MCP_BASE_URL", "http://localhost:9090")  # 可通过环境变量覆盖

# 其他配置项
DEFAULT_TIMEOUT = int(os.environ.get("MCP_TIMEOUT", "10"))  # 请求超时时间（秒）
DEBUG = True  # 调试模式

# 认证配置
# 有些API可能需要不同的认证头格式
# AUTH_HEADER_FORMAT = "Bearer {token}"  # 标准格式，包含Bearer前缀
AUTH_HEADER_FORMAT = "{token}"       # 简单格式，没有Bearer前缀
# AUTH_HEADER_FORMAT = None            # 使用自定义格式 