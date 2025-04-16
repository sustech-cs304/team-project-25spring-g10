"""
会话管理模块，负责管理用户的认证状态和令牌
"""

class SessionManager:
    """
    会话管理类，处理用户的认证状态、令牌存储和访问
    """
    def __init__(self):
        """初始化会话管理器"""
        self.token = None
        self.user_info = None
        self.is_logged_in = False
    
    def set_token(self, token):
        """
        设置用户令牌并更新登录状态
        
        Args:
            token (str): JWT令牌
        """
        self.token = token
        self.is_logged_in = True
    
    def clear_token(self):
        """清除用户令牌和登录状态"""
        self.token = None
        self.is_logged_in = False
        self.user_info = None
    
    def get_token(self):
        """
        获取当前存储的令牌
        
        Returns:
            str or None: 当前存储的JWT令牌，未登录时为None
        """
        return self.token
    
    def is_authenticated(self):
        """
        检查用户是否已认证
        
        Returns:
            bool: 用户是否已认证
        """
        return self.is_logged_in and self.token is not None

    def set_user_info(self, user_info):
        """
        存储用户信息
        
        Args:
            user_info (dict): 用户信息
        """
        self.user_info = user_info
    
    def get_user_info(self):
        """
        获取存储的用户信息
        
        Returns:
            dict or None: 用户信息，未存储时为None
        """
        return self.user_info

# 创建全局会话管理器实例
session = SessionManager() 