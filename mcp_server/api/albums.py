"""
相册管理模块，提供创建、获取、更新、删除相册等功能
"""
from mcp_server.utils.decorators import requires_token
from mcp_server.utils.http_utils import api_get, api_post, api_put, api_delete

def register_tools(mcp):
    """
    向MCP服务器注册所有相册管理相关工具
    
    Args:
        mcp: MCP服务器实例
    """
    mcp.tool()(get_all_albums)
    mcp.tool()(create_album)
    mcp.tool()(get_album)
    mcp.tool()(update_album)
    mcp.tool()(delete_album)
    mcp.tool()(get_album_photos)

@requires_token
def get_all_albums() -> dict:
    """
    获取当前用户的所有相册

    Returns:
        dict: 包含所有相册信息的响应
    """
    return api_get("/api/albums")

@requires_token
def create_album(name: str, description: str = None) -> dict:
    """
    创建新相册

    Args:
        name: 相册名称
        description: 相册描述（可选）

    Returns:
        dict: 包含新创建的相册信息的响应
    """
    data = {
        "name": name
    }
    if description:
        data["description"] = description
        
    return api_post("/api/albums", json_data=data)

@requires_token
def get_album(album_id: int) -> dict:
    """
    获取单个相册的详细信息

    Args:
        album_id: 相册ID

    Returns:
        dict: 包含相册详细信息的响应
    """
    return api_get(f"/api/albums/{album_id}")

@requires_token
def update_album(album_id: int, name: str = None, description: str = None) -> dict:
    """
    更新相册信息

    Args:
        album_id: 相册ID
        name: 新相册名称（可选）
        description: 新相册描述（可选）

    Returns:
        dict: 包含更新后的相册信息的响应
    """
    # 只包含非空参数
    data = {}
    if name:
        data["name"] = name
    if description:
        data["description"] = description
        
    return api_put(f"/api/albums/{album_id}", json_data=data)

@requires_token
def delete_album(album_id: int) -> dict:
    """
    删除相册（照片会移至垃圾桶）

    Args:
        album_id: 要删除的相册ID

    Returns:
        dict: 包含删除操作结果的响应
    """
    return api_delete(f"/api/albums/{album_id}")

@requires_token
def get_album_photos(album_id: int) -> dict:
    """
    获取指定相册中的所有照片

    Args:
        album_id: 相册ID

    Returns:
        dict: 包含相册中照片列表的响应
    """
    return api_get(f"/api/albums/{album_id}/photos") 