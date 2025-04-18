"""
照片管理模块，提供获取、上传、删除、移动等照片相关功能
"""
from mcp_server.utils.decorators import requires_token
from mcp_server.utils.http_utils import api_get, api_post, api_put, api_delete

def register_tools(mcp):
    """
    向MCP服务器注册所有照片管理相关工具
    
    Args:
        mcp: MCP服务器实例
    """
    mcp.tool()(get_all_photos)
    mcp.tool()(get_photo_detail)
    mcp.tool()(upload_photo)
    mcp.tool()(delete_photo)
    mcp.tool()(move_photo)
    mcp.tool()(search_photos)

@requires_token
def get_all_photos() -> dict:
    """
    获取当前用户的所有照片

    Returns:
        dict: 包含所有照片信息的响应
    """
    return api_get("/api/photo")

@requires_token
def get_photo_detail(photo_id: int) -> dict:
    """
    获取照片详情

    Args:
        photo_id: 照片ID

    Returns:
        dict: 包含照片详细信息的响应
    """
    return api_get(f"/api/photo/{photo_id}")

@requires_token
def upload_photo(file_path: str, album_id: int) -> dict:
    """
    上传照片

    Args:
        file_path: 图片文件的本地路径
        album_id: 相册ID

    Returns:
        dict: 包含上传后照片信息的响应
    """
    # 简化版实现，假设file_path是有效的本地文件路径
    import os
    import mimetypes
    from mcp_server.config import DEBUG, BASE_URL
    from mcp_server.utils.http_utils import get_auth_header
    import requests
    
    if not os.path.exists(file_path):
        return {"error": "文件不存在", "status": "error"}
    
    # 检查文件大小（限制为10MB）
    file_size = os.path.getsize(file_path)
    if file_size > 10 * 1024 * 1024:
        return {"error": "文件大小不能超过10MB", "status": "error"}
    
    # 检查文件类型
    content_type, _ = mimetypes.guess_type(file_path)
    if not content_type:
        # 根据文件扩展名强制设置content_type
        ext = os.path.splitext(file_path)[1].lower()
        if ext == '.jpg' or ext == '.jpeg':
            content_type = 'image/jpeg'
        elif ext == '.png':
            content_type = 'image/png'
        elif ext == '.gif':
            content_type = 'image/gif'
        else:
            return {"error": f"只支持jpg、png、gif格式的图片，当前文件: {file_path}", "status": "error"}
    
    if not (content_type.startswith('image/jpeg') or 
            content_type.startswith('image/png') or 
            content_type.startswith('image/gif')):
        return {"error": f"只支持jpg、png、gif格式的图片，当前类型: {content_type}", "status": "error"}
    
    # 构建请求URL和头部
    url = f"{BASE_URL}/api/photo/upload"
    headers = get_auth_header()
    # 删除Content-Type以让requests自动设置multipart边界
    if 'Content-Type' in headers:
        del headers['Content-Type']
    
    # 添加Accept头，表明客户端期望JSON响应
    headers['Accept'] = 'application/json'
    
    try:
        # 打开文件并构建multipart/form-data请求
        with open(file_path, 'rb') as f:
            filename = os.path.basename(file_path)
            # 确保文件名不包含特殊字符
            import re
            safe_filename = re.sub(r'[^\w\.-]', '_', filename)
            
            # 明确指定文件类型
            files = {'file': (safe_filename, f, content_type)}
            # 确保albumId作为字符串发送
            data = {'albumId': str(album_id)}
            
            if DEBUG:
                print(f"\nDEBUG - 上传文件: {safe_filename}")
                print(f"DEBUG - 原始文件名: {filename}")
                print(f"DEBUG - 文件大小: {file_size} 字节")
                print(f"DEBUG - 文件类型: {content_type}")
                print(f"DEBUG - URL: {url}")
                print(f"DEBUG - 请求头: {headers}")
                print(f"DEBUG - 表单数据: {data}")
                
                # 以下是文件信息的打印
                print(f"DEBUG - 文件对象类型: {type(f)}")
                print(f"DEBUG - 文件参数: {files}")
                
                # 打印请求结构
                print("\nDEBUG - 完整请求结构:")
                print(f"  URL: {url}")
                print(f"  方法: POST")
                print(f"  头部: {headers}")
                print(f"  数据: {data}")
                print(f"  文件: {{'file': ('{safe_filename}', <file object>, '{content_type}')}}")
            
            # 发送请求
            response = requests.post(
                url=url,
                headers=headers,
                files=files,
                data=data,
                timeout=30  # 增加上传超时时间
            )
            
            
            if DEBUG:
                print(f"\nDEBUG - 响应状态码: {response.status_code}")
                print(f"DEBUG - 响应头: {dict(response.headers)}")
                try:
                    print(f"DEBUG - 响应内容: {response.text[:200]}")
                except:
                    print("DEBUG - 无法显示响应内容")
                    
                # 添加请求的详细信息
                try:
                    print("\nDEBUG - 请求详情:")
                    if hasattr(response, 'request'):
                        print(f"  请求URL: {response.request.url}")
                        print(f"  请求方法: {response.request.method}")
                        print(f"  请求头: {dict(response.request.headers)}")
                        print(f"  请求体预览: {str(response.request.body)[:500] if response.request.body else '无请求体'}")
                except Exception as e:
                    print(f"DEBUG - 无法打印请求详情: {str(e)}")
            
            # 处理响应
            if response.status_code != 200:
                try:
                    error_json = response.json()
                    return {"error": f"上传失败: {error_json.get('message', '未知错误')}", "status": "error"}
                except:
                    return {
                        "error": f"上传失败: 服务器返回状态码 {response.status_code}，响应内容: {response.text[:100] if response.text else '空响应'}",
                        "status": "error"
                    }
            
            # 尝试解析JSON响应
            try:
                return response.json()
            except ValueError as e:
                if response.text:
                    return {"error": f"上传可能成功但返回格式无效: {str(e)}, 响应: {response.text[:100]}", "status": "warning"}
                else:
                    return {"error": "上传失败: 服务器返回空响应", "status": "error"}
    except Exception as e:
        import traceback
        error_details = traceback.format_exc() if DEBUG else str(e)
        return {"error": f"上传失败: {error_details}", "status": "error"}

@requires_token
def delete_photo(photo_id: int) -> dict:
    """
    删除照片（将移至回收站）

    Args:
        photo_id: 要删除的照片ID

    Returns:
        dict: 包含删除操作结果的响应
    """
    return api_delete(f"/api/photo/{photo_id}")

@requires_token
def move_photo(photo_id: int, target_album_id: int) -> dict:
    """
    移动照片到新相册

    Args:
        photo_id: 照片ID
        target_album_id: 目标相册ID

    Returns:
        dict: 包含移动操作结果的响应
    """
    data = {"id": target_album_id}
    return api_put(f"/api/photo/{photo_id}/move", json_data=data)

@requires_token
def search_photos(keyword: str, album_id: int = None, page: int = 1, size: int = 10) -> dict:
    """
    搜索照片

    Args:
        keyword: 搜索关键词
        album_id: 相册ID（可选）
        page: 页码（默认1）
        size: 每页数量（默认10）

    Returns:
        dict: 包含搜索结果的响应
    """
    params = {
        "keyword": keyword,
        "page": page,
        "size": size
    }
    
    if album_id is not None:
        params["albumId"] = album_id
        
    return api_get("/api/photos/search", params=params) 