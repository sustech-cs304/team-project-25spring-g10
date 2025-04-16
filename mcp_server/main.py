from mcp.server.fastmcp import FastMCP
import os
import requests
import json
from functools import wraps
from typing import Optional, Dict, Any

# Create an MCP server
mcp = FastMCP("La-Album API Tools")

# 基础URL
BASE_URL = "http://localhost:9090"  # 根据实际情况修改

# 会话管理类
class SessionManager:
    def __init__(self):
        self.token = None
        self.user_info = None
        self.is_logged_in = False
    
    def set_token(self, token):
        self.token = token
        self.is_logged_in = True
    
    def clear_token(self):
        self.token = None
        self.is_logged_in = False
        self.user_info = None
    
    def get_token(self):
        return self.token
    
    def is_authenticated(self):
        return self.is_logged_in and self.token is not None

# 创建全局会话管理器
session = SessionManager()

# 需要令牌的装饰器
def requires_token(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        # 使用会话中的令牌
        if not session.is_authenticated():
            return {"error": "未登录，请先登录获取令牌", "status": "error"}
        return func(*args, **kwargs)
    return wrapper

# 用户注册工具
@mcp.tool()
def register_user(username: str, password: str) -> dict:
    """
    注册新用户

    Args:
        username: 用户名（5-16位非空字符）
        password: 密码（5-16位非空字符）

    Returns:
        dict: 包含注册结果的响应
    """
    url = f"{BASE_URL}/api/users/register"
    data = {
        "username": username,
        "password": password
    }
    response = requests.post(url, data=data)
    return response.json()

# 用户登录工具 - 登录后自动保存令牌
@mcp.tool()
def login_user(username: str, password: str) -> dict:
    """
    用户登录并获取JWT令牌，登录成功后自动保存令牌

    Args:
        username: 用户名
        password: 密码

    Returns:
        dict: 登录结果，包含状态和消息
    """
    url = f"{BASE_URL}/api/users/login"
    data = {
        "username": username,
        "password": password
    }
    response = requests.post(url, data=data)
    result = response.json()
    
    # 如果登录成功，保存令牌
    if result.get('code') == 0 and result.get('data'):
        session.set_token(result['data'])
        return {"status": "success", "message": "登录成功", "token": result['data'], "original_response": result}
    return {"status": "error", "message": "登录失败", "error": result.get('message'), "original_response": result}

# 用户登出工具 - 自动清除令牌
@mcp.tool()
@requires_token
def logout_user() -> dict:
    """
    用户登出，自动清除令牌

    Returns:
        dict: 登出操作的结果
    """
    url = f"{BASE_URL}/api/users/logout"
    headers = {
        "Authorization": f"{session.get_token()}"
    }
    response = requests.post(url, headers=headers)
    result = response.json()
    
    # 不管结果如何，清除会话
    session.clear_token()
    return {"status": "success", "message": "已登出", "original_response": result}

# 获取用户信息工具 - 自动使用令牌
@mcp.tool()
@requires_token
def get_user_info() -> dict:
    """
    获取当前用户信息，自动使用保存的令牌

    Returns:
        dict: 包含用户信息的响应
    """
    url = f"{BASE_URL}/api/users/userInfo"
    headers = {
        "Authorization": f"{session.get_token()}"
    }
    response = requests.get(url, headers=headers)
    return response.json()

# 更新用户信息工具 - 自动使用令牌
@mcp.tool()
@requires_token
def update_user_info(username: str = None, password: str = None, userPic: str = None) -> dict:
    """
    更新用户信息，自动使用保存的令牌

    Args:
        username: 新用户名（可选）
        password: 新密码（可选）
        userPic: 用户头像URL（可选）

    Returns:
        dict: 更新操作的响应
    """
    url = f"{BASE_URL}/api/users/update"
    headers = {
        "Authorization": f"{session.get_token()}",
        "Content-Type": "application/json"
    }
    
    # 只包含非空参数
    data = {}
    if username:
        data["username"] = username
    if password:
        data["password"] = password
    if userPic:
        data["userPic"] = userPic
        
    response = requests.put(url, headers=headers, json=data)
    return response.json()

# 更新用户头像工具 - 自动使用令牌
@mcp.tool()
@requires_token
def update_user_avatar(avatarUrl: str) -> dict:
    """
    更新用户头像，自动使用保存的令牌

    Args:
        avatarUrl: 头像URL

    Returns:
        dict: 更新头像操作的响应
    """
    url = f"{BASE_URL}/api/users/updateAvatar"
    headers = {
        "Authorization": f"{session.get_token()}"
    }
    data = {
        "avatarUrl": avatarUrl
    }
    response = requests.patch(url, headers=headers, data=data)
    return response.json()

# 检查登录状态工具
@mcp.tool()
def check_login_status() -> dict:
    """
    检查当前用户的登录状态

    Returns:
        dict: 包含登录状态的信息
    """
    if session.is_authenticated():
        return {
            "status": "authenticated", 
            "message": "用户已登录", 
            "token_available": True
        }
    else:
        return {
            "status": "unauthenticated", 
            "message": "用户未登录或令牌已过期", 
            "token_available": False
        }

# @mcp.prompt()
# def modify_code_prompt(original_prompt: str = "") -> str:
#     """
#     Append "请阅读代码，分段实现" to the original prompt

#     Args:
#         original_prompt: The original prompt text

#     Returns:
#         str: A prompt string that appends "请阅读代码，分段实现" to the original prompt
#     """
#     if original_prompt:
#         return f"{original_prompt} 请阅读代码，分段实现"
#     else:
#         return "请阅读代码，分段实现"

if __name__ == "__main__":
    mcp.run(transport="stdio")