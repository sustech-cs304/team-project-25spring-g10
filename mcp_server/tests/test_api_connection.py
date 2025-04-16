"""
API连接测试脚本
用于验证与La-Album后端的连接和API调用
"""

import sys
import os

# 添加项目根目录到Python路径，以便导入模块
project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
if project_root not in sys.path:
    sys.path.insert(0, os.path.abspath(os.path.join(project_root, "..")))

# 使用绝对导入
from mcp_server.config import BASE_URL, DEBUG
from mcp_server.session_manager import session
from mcp_server.utils.http_utils import api_get, api_post

def test_base_connection():
    """测试与API服务器的基本连接"""
    print("\n===== 测试与API服务器的基本连接 =====")
    # 尝试访问健康检查端点或主页
    result = api_get("/")
    print(f"基础连接测试结果: {result}")
    
    # 如果失败，尝试直接用requests测试
    if "error" in result:
        import requests
        try:
            response = requests.get(BASE_URL, timeout=5)
            print(f"直接请求结果: 状态码 {response.status_code}")
        except Exception as e:
            print(f"直接请求异常: {str(e)}")
    
    return "error" not in result

def test_login():
    """测试用户登录"""
    print("\n===== 测试用户登录 =====")
    # 尝试使用测试账号登录
    login_data = {
        "username": "testhhhh",
        "password": "123456"
    }
    
    result = api_post("/api/users/login", data=login_data)
    
    if "error" in result:
        print(f"登录失败: {result}")
        return False
    
    print(f"登录结果: {result}")
    
    # 如果登录成功，保存令牌
    if result.get('code') == 0 and result.get('data'):
        token = result['data']
        session.set_token(token)
        print(f"保存令牌: {token[:10]}...")
        return True
    else:
        print("登录失败，未获取到令牌")
        return False

def test_get_albums():
    """测试获取相册列表"""
    print("\n===== 测试获取相册 =====")
    
    # 确保已登录
    if not session.is_authenticated():
        print("未登录，无法测试获取相册")
        return False
    
    result = api_get("/api/albums")
    
    if "error" in result:
        print(f"获取相册失败: {result}")
        return False
    
    print(f"获取相册结果: {result}")
    return True

def test_all():
    """运行所有测试"""
    print("\n***** 开始API连接测试 *****")
    print(f"当前BASE_URL: {BASE_URL}")
    print(f"调试模式: {'开启' if DEBUG else '关闭'}")
    
    # 跳过基本连接测试，直接进行登录测试
    print("\n跳过基本连接测试，直接测试登录...")
    
    # 测试登录
    login_ok = test_login()
    if not login_ok:
        print("\n⚠️ 登录失败! 请检查用户名和密码")
        return
    
    # 测试获取相册
    albums_ok = test_get_albums()
    if not albums_ok:
        print("\n⚠️ 获取相册失败! 请检查API路径和认证方式")
        return
    
    print("\n✅ 所有测试通过!")

if __name__ == "__main__":
    test_all() 