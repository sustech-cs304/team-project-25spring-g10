"""
装饰器模块，提供各种用于API调用的装饰器
"""
from functools import wraps
from mcp_server.session_manager import session

def requires_token(func):
    """
    验证用户是否已登录的装饰器
    
    如果用户未登录，返回错误信息，否则执行原函数
    
    Args:
        func: 被装饰的函数
        
    Returns:
        函数: 包装后的函数
    """
    @wraps(func)
    def wrapper(*args, **kwargs):
        # 使用会话中的令牌
        if not session.is_authenticated():
            return {"error": "未登录，请先登录获取令牌", "status": "error"}
        return func(*args, **kwargs)
    return wrapper

def debug_log(func):
    """
    记录函数调用日志的装饰器
    
    Args:
        func: 被装饰的函数
        
    Returns:
        函数: 包装后的函数
    """
    @wraps(func)
    def wrapper(*args, **kwargs):
        print(f"DEBUG: 调用 {func.__name__} 函数")
        print(f"DEBUG: 参数: {args}, {kwargs}")
        result = func(*args, **kwargs)
        print(f"DEBUG: {func.__name__} 返回: {result}")
        return result
    return wrapper 