"""
HTTP请求工具模块，提供封装好的请求函数
"""
import requests
import json
import mcp_server.session_manager as session_module
from mcp_server.config import BASE_URL, DEFAULT_TIMEOUT, DEBUG, AUTH_HEADER_FORMAT

# 从导入的模块中获取session对象
session = session_module.session

def get_auth_header():
    """
    生成带有认证信息的请求头
    
    Returns:
        dict: 包含认证信息的请求头
    """
    token = session.get_token()
    if not token:
        return {}
    
    if DEBUG:
        print(f"DEBUG - 使用令牌: {token[:10]}...")
    
    # 根据配置的格式返回不同的认证头
    if AUTH_HEADER_FORMAT is None:
        return {"token": token}  # 一些API使用此格式
    else:
        # 使用配置的格式化字符串
        auth_value = AUTH_HEADER_FORMAT.format(token=token)
        return {"Authorization": auth_value}

def print_debug_info(prefix, url, headers, response=None, error=None):
    """打印调试信息"""
    if not DEBUG:
        return
        
    print(f"\n==== {prefix} DEBUG INFO ====")
    print(f"URL: {url}")
    print(f"Headers: {headers}")
    
    if response:
        print(f"Status Code: {response.status_code}")
        print(f"Response Headers: {dict(response.headers)}")
        print(f"Response Content Type: {response.headers.get('Content-Type', 'None')}")
        
        # 尝试显示部分响应内容
        try:
            content_preview = response.text[:200] + "..." if len(response.text) > 200 else response.text
            print(f"Response Content: {content_preview}")
        except:
            print("无法显示响应内容")
            
    if error:
        print(f"Error: {error}")
        
    print("==== DEBUG INFO END ====\n")

def api_get(endpoint, params=None):
    """
    执行GET请求
    
    Args:
        endpoint (str): API端点路径
        params (dict, optional): 查询参数
        
    Returns:
        dict: 响应JSON
    """
    url = f"{BASE_URL}{endpoint}"
    headers = get_auth_header()
    
    if DEBUG:
        print(f"\nDEBUG - 发送GET请求: {url}")
        print(f"DEBUG - 查询参数: {params}")
    
    try:
        response = requests.get(url, headers=headers, params=params, timeout=DEFAULT_TIMEOUT)
        print_debug_info("GET", url, headers, response)
        
        return response.json()
    except requests.RequestException as e:
        print_debug_info("GET ERROR", url, headers, error=str(e))
        return {"error": f"请求异常: {str(e)}", "status": "error"}
    except ValueError as e:
        if 'response' in locals():
            print_debug_info("GET JSON ERROR", url, headers, response, error=str(e))
            return {"error": f"JSON解析错误: {str(e)}, 响应状态码: {response.status_code}", "status": "error", "raw_response": response.text[:500]}
        else:
            print_debug_info("GET UNKNOWN ERROR", url, headers, error=str(e))
            return {"error": f"未知错误: {str(e)}", "status": "error"}

def api_post(endpoint, data=None, json_data=None):
    """
    执行POST请求
    
    Args:
        endpoint (str): API端点路径
        data (dict, optional): 表单数据
        json_data (dict, optional): JSON数据
        
    Returns:
        dict: 响应JSON
    """
    url = f"{BASE_URL}{endpoint}"
    headers = get_auth_header()
    if json_data:
        headers["Content-Type"] = "application/json"
    
    if DEBUG:
        print(f"\nDEBUG - 发送POST请求: {url}")
        print(f"DEBUG - 表单数据: {data}")
        print(f"DEBUG - JSON数据: {json_data}")
    
    try:
        response = requests.post(url, headers=headers, data=data, json=json_data, timeout=DEFAULT_TIMEOUT)
        print_debug_info("POST", url, headers, response)
        
        return response.json()
    except requests.RequestException as e:
        print_debug_info("POST ERROR", url, headers, error=str(e))
        return {"error": f"请求异常: {str(e)}", "status": "error"}
    except ValueError as e:
        if 'response' in locals():
            print_debug_info("POST JSON ERROR", url, headers, response, error=str(e))
            return {"error": f"JSON解析错误: {str(e)}, 响应状态码: {response.status_code}", "status": "error", "raw_response": response.text[:500]}
        else:
            print_debug_info("POST UNKNOWN ERROR", url, headers, error=str(e))
            return {"error": f"未知错误: {str(e)}", "status": "error"}

def api_put(endpoint, data=None, json_data=None):
    """
    执行PUT请求
    
    Args:
        endpoint (str): API端点路径
        data (dict, optional): 表单数据
        json_data (dict, optional): JSON数据
        
    Returns:
        dict: 响应JSON
    """
    url = f"{BASE_URL}{endpoint}"
    headers = get_auth_header()
    if json_data:
        headers["Content-Type"] = "application/json"
    
    if DEBUG:
        print(f"\nDEBUG - 发送PUT请求: {url}")
        print(f"DEBUG - 表单数据: {data}")
        print(f"DEBUG - JSON数据: {json_data}")
    
    try:
        response = requests.put(url, headers=headers, data=data, json=json_data, timeout=DEFAULT_TIMEOUT)
        print_debug_info("PUT", url, headers, response)
        
        return response.json()
    except requests.RequestException as e:
        print_debug_info("PUT ERROR", url, headers, error=str(e))
        return {"error": f"请求异常: {str(e)}", "status": "error"}
    except ValueError as e:
        if 'response' in locals():
            print_debug_info("PUT JSON ERROR", url, headers, response, error=str(e))
            return {"error": f"JSON解析错误: {str(e)}, 响应状态码: {response.status_code}", "status": "error", "raw_response": response.text[:500]}
        else:
            print_debug_info("PUT UNKNOWN ERROR", url, headers, error=str(e))
            return {"error": f"未知错误: {str(e)}", "status": "error"}

def api_delete(endpoint):
    """
    执行DELETE请求
    
    Args:
        endpoint (str): API端点路径
        
    Returns:
        dict: 响应JSON
    """
    url = f"{BASE_URL}{endpoint}"
    headers = get_auth_header()
    
    if DEBUG:
        print(f"\nDEBUG - 发送DELETE请求: {url}")
    
    try:
        response = requests.delete(url, headers=headers, timeout=DEFAULT_TIMEOUT)
        print_debug_info("DELETE", url, headers, response)
        
        return response.json()
    except requests.RequestException as e:
        print_debug_info("DELETE ERROR", url, headers, error=str(e))
        return {"error": f"请求异常: {str(e)}", "status": "error"}
    except ValueError as e:
        if 'response' in locals():
            print_debug_info("DELETE JSON ERROR", url, headers, response, error=str(e))
            return {"error": f"JSON解析错误: {str(e)}, 响应状态码: {response.status_code}", "status": "error", "raw_response": response.text[:500]}
        else:
            print_debug_info("DELETE UNKNOWN ERROR", url, headers, error=str(e))
            return {"error": f"未知错误: {str(e)}", "status": "error"} 