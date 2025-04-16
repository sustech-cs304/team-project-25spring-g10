"""
MCP服务器主入口文件，负责创建服务器实例并注册所有工具和提示
"""
from mcp.server.fastmcp import FastMCP
import os
import sys

# 添加项目根目录到Python路径
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
if parent_dir not in sys.path:
    sys.path.insert(0, parent_dir)

# 导入各个模块
from mcp_server.api import auth, albums
from mcp_server.prompts import code_prompts

# 创建MCP服务器
mcp = FastMCP("La-Album API Tools")

# 注册所有工具和提示
def register_all():
    """注册所有工具和提示到MCP服务器"""
    # 注册用户认证相关工具
    auth.register_tools(mcp)
    
    # 注册相册管理相关工具
    albums.register_tools(mcp)
    
    # 注册提示功能
    code_prompts.register_prompts(mcp)

# 注册所有功能
register_all()

if __name__ == "__main__":
    print("La-Album API Tools MCP服务器启动中...")
    mcp.run(transport="stdio")