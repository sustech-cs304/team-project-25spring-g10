"""
用户认证模块，提供用户登录、注册、获取用户信息等功能
"""
from mcp_server.utils.decorators import requires_token
from mcp_server.utils.http_utils import api_get, api_post, api_put, api_delete
from mcp_server.session_manager import session

def register_tools(mcp):
    """
    向MCP服务器注册所有用户认证相关工具
    
    Args:
        mcp: MCP服务器实例
    """
    mcp.tool()(register_user)
    mcp.tool()(login_user)
    mcp.tool()(logout_user)
    mcp.tool()(get_user_info)
    mcp.tool()(update_user_info)
    mcp.tool()(update_user_avatar)
    mcp.tool()(check_login_status)

def register_user(username: str, password: str) -> dict:
    """
    注册新用户

    Args:
        username: 用户名（5-16位非空字符）
        password: 密码（5-16位非空字符）

    Returns:
        dict: 包含注册结果的响应
    """
    data = {
        "username": username,
        "password": password
    }
    return api_post("/api/users/register", data=data)

def login_user(username: str, password: str) -> dict:
    """
    用户登录并获取JWT令牌，登录成功后自动保存令牌

    Args:
        username: 用户名
        password: 密码

    Returns:
        dict: 登录结果，包含状态和消息
    """
    data = {
        "username": username,
        "password": password
    }
    result = api_post("/api/users/login", data=data)
    
    # 如果登录成功，保存令牌
    if result.get('code') == 0 and result.get('data'):
        session.set_token(result['data'])
        return {"status": "success", "message": "登录成功", "token": result['data'], "original_response": result}
    return {"status": "error", "message": "登录失败", "error": result.get('message'), "original_response": result}

@requires_token
def logout_user() -> dict:
    """
    用户登出，自动清除令牌

    Returns:
        dict: 登出操作的结果
    """
    result = api_post("/api/users/logout")
    
    # 不管结果如何，清除会话
    session.clear_token()
    return {"status": "success", "message": "已登出", "original_response": result}

@requires_token
def get_user_info() -> dict:
    """
    获取当前用户信息，自动使用保存的令牌

    Returns:
        dict: 包含用户信息的响应
    """
    result = api_get("/api/users/userInfo")
    
    # 如果成功，将用户信息保存到会话中
    if result.get('code') == 0 and result.get('data'):
        session.set_user_info(result['data'])
    
    return result

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
    # 只包含非空参数
    data = {}
    if username:
        data["username"] = username
    if password:
        data["password"] = password
    if userPic:
        data["userPic"] = userPic
        
    return api_put("/api/users/update", json_data=data)

@requires_token
def update_user_avatar(avatarUrl: str) -> dict:
    """
    更新用户头像，自动使用保存的令牌

    Args:
        avatarUrl: 头像URL

    Returns:
        dict: 更新头像操作的响应
    """
    data = {
        "avatarUrl": avatarUrl
    }
    return api_post("/api/users/updateAvatar", data=data)

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