# La-Album API 文档
BASE_URL = "http://localhost:9090"  !!!!!!!
## 1. 用户认证模块

### 1.1 用户注册
- **URL**: `/api/users/register`
- **Method**: `POST`
- **Headers**: 
  - `Content-Type: application/x-www-form-urlencoded`
- **Request Parameters**:
  - `username`: 用户名（5-16位非空字符）
  - `password`: 密码（5-16位非空字符）
- **Response**:
```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```
- **Error Codes**:
  - 400: 参数错误（用户名或密码格式不正确）
  - 409: 用户名已被占用
  - 500: 服务器错误

### 1.2 用户登录
- **URL**: `/api/users/login`
- **Method**: `POST`
- **Headers**: 
  - `Content-Type: application/x-www-form-urlencoded`
- **Request Parameters**:
  - `username`: 用户名
  - `password`: 密码
- **Response**:
```json
{
    "code": 200,
    "message": "success",
    "data": "string"  // JWT token
}
```
- **Error Codes**:
  - 400: 参数错误
  - 401: 用户名或密码错误
  - 500: 服务器错误

### 1.3 用户登出
- **URL**: `/api/users/logout`
- **Method**: `POST`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Response**:
```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```
- **Error Codes**:
  - 401: 未授权
  - 500: 服务器错误

### 1.4 获取当前用户信息
- **URL**: `/api/users/userInfo`
- **Method**: `GET`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Response**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": "number",
        "username": "string",
        "userPic": "string"  // 用户头像URL
    }
}
```
- **Error Codes**:
  - 401: 未授权
  - 500: 服务器错误

### 1.5 更新用户信息
- **URL**: `/api/users/update`
- **Method**: `PUT`
- **Headers**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: application/json`
- **Request Body**:
```json
{
    "username": "string",  // 可选
    "password": "string",  // 可选
    "userPic": "string"    // 可选
}
```
- **Response**:
```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```
- **Error Codes**:
  - 400: 参数错误
  - 401: 未授权
  - 409: 用户名已存在
  - 500: 服务器错误

### 1.6 更新用户头像
- **URL**: `/api/users/updateAvatar`
- **Method**: `PATCH`
- **Headers**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: application/x-www-form-urlencoded`
- **Request Parameters**:
  - `avatarUrl`: 头像URL
- **Response**:
```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```
- **Error Codes**:
  - 401: 未授权
  - 404: 用户不存在
  - 500: 服务器错误

## 2. 相册管理模块

### 2.1 获取所有相册
- **URL**: `/api/albums`
- **Method**: `GET`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Response**:
```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "id": "number",
            "name": "string",
            "description": "string",
            "createTime": "string",
            "updateTime": "string",
            "user": {
                "id": "number",
                "username": "string"
            }
        }
    ]
}
```
- **Error Codes**:
  - 401: 未授权
  - 500: 服务器错误

### 2.2 创建相册
- **URL**: `/api/albums`
- **Method**: `POST`
- **Headers**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: application/json`
- **Request Body**:
```json
{
    "name": "string",        // 相册名称
    "description": "string"  // 相册描述（可选）
}
```
- **Response**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": "number",
        "name": "string",
        "description": "string",
        "createTime": "string",
        "updateTime": "string",
        "user": {
            "id": "number",
            "username": "string"
        }
    }
}
```
- **Error Codes**:
  - 401: 未授权
  - 500: 服务器错误

### 2.3 获取单个相册
- **URL**: `/api/albums/{id}`
- **Method**: `GET`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Response**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": "number",
        "name": "string",
        "description": "string",
        "createTime": "string",
        "updateTime": "string",
        "user": {
            "id": "number",
            "username": "string"
        }
    }
}
```
- **Error Codes**:
  - 401: 未授权
  - 403: 无权访问
  - 404: 相册不存在
  - 500: 服务器错误

### 2.4 更新相册
- **URL**: `/api/albums/{id}`
- **Method**: `PUT`
- **Headers**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: application/json`
- **Request Body**:
```json
{
    "name": "string",        // 可选
    "description": "string"  // 可选
}
```
- **Response**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": "number",
        "name": "string",
        "description": "string",
        "createTime": "string",
        "updateTime": "string",
        "user": {
            "id": "number",
            "username": "string"
        }
    }
}
```
- **Error Codes**:
  - 401: 未授权
  - 403: 无权修改
  - 404: 相册不存在
  - 500: 服务器错误

### 2.5 删除相册
- **URL**: `/api/albums/{id}`
- **Method**: `DELETE`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Response**:
```json
{
    "code": 200,
    "message": "success",
    "data": "相册已删除，照片已移至垃圾桶"
}
```
- **Error Codes**:
  - 401: 未授权
  - 403: 无权删除
  - 404: 相册不存在
  - 500: 服务器错误

### 2.6 获取相册中的照片
- **URL**: `/api/albums/{albumId}/photos`
- **Method**: `GET`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Response**:
```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "id": "number",
            "name": "string",
            "url": "string",      // 带签名的URL
            "description": "string",
            "createTime": "string",
            "updateTime": "string"
        }
    ]
}
```
- **Error Codes**:
  - 401: 未授权
  - 403: 无权访问
  - 404: 相册不存在
  - 500: 服务器错误

## 3. 照片管理模块

### 3.1 获取所有照片
- **URL**: `/api/photo`
- **Method**: `GET`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Response**:
```json
[
    {
        "id": "number",
        "title": "string",
        "url": "string",      // 带签名的URL
        "album": {
            "id": "number",
            "name": "string"
        }
    }
]
```
- **Error Codes**:
  - 401: 未授权
  - 500: 服务器错误

### 3.2 获取照片详情
- **URL**: `/api/photo/{id}`
- **Method**: `GET`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Response**:
```json
{
    "id": "number",
    "title": "string",
    "url": "string",      // 带签名的URL
    "album": {
        "id": "number",
        "name": "string"
    }
}
```
- **Error Codes**:
  - 401: 未授权
  - 404: 照片不存在
  - 500: 服务器错误

### 3.3 上传照片
- **URL**: `/api/photo/upload`
- **Method**: `POST`
- **Headers**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: multipart/form-data`
- **Request Parameters**:
  - `file`: 图片文件（jpg, png, gif，最大10MB）
  - `albumId`: 相册ID
- **Response**:
```json
{
    "id": "number",
    "title": "string",
    "url": "string",
    "album": {
        "id": "number",
        "name": "string"
    }
}
```
- **Error Codes**:
  - 400: 参数错误（文件为空、文件太大、不支持的文件类型）
  - 401: 未授权
  - 500: 服务器错误

### 3.4 删除照片
- **URL**: `/api/photo/{id}`
- **Method**: `DELETE`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Response**:
  - 状态码：204 No Content
- **Error Codes**:
  - 401: 未授权
  - 404: 照片不存在
  - 500: 服务器错误

### 3.5 移动照片到新相册
- **URL**: `/api/photo/{id}/move`
- **Method**: `PUT`
- **Headers**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: application/json`
- **Request Body**:
```json
{
    "id": "number"  // 目标相册ID
}
```
- **Response**:
  - 状态码：200 OK
- **Error Codes**:
  - 401: 未授权
  - 404: 照片或相册不存在
  - 500: 服务器错误

### 3.6 搜索照片
- **URL**: `/api/photos/search`
- **Method**: `GET`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Query Parameters**:
  - `keyword`: 搜索关键词
  - `albumId`: 相册ID（可选）
  - `page`: 页码（默认1）
  - `size`: 每页数量（默认10）
- **Response**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": "number",
        "list": [
            {
                "id": "number",
                "name": "string",
                "url": "string",      // 带签名的URL
                "description": "string",
                "createTime": "string",
                "updateTime": "string",
                "album": {
                    "id": "number",
                    "name": "string"
                }
            }
        ]
    }
}
```
- **Error Codes**:
  - 401: 未授权
  - 500: 服务器错误

## 4. 回收站管理模块

### 4.1 获取回收站中的照片
- **URL**: `/api/trash/{trashBinId}/photos`
- **Method**: `GET`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Response**:
```json
[
    {
        "id": "number",
        "title": "string",
        "url": "string",
        "deleteTime": "string",
        "originalAlbum": {
            "id": "number",
            "name": "string"
        }
    }
]
```
- **Error Codes**:
  - 401: 未授权
  - 404: 回收站不存在
  - 500: 服务器错误

### 4.2 还原照片
- **URL**: `/api/trash/restore/{id}`
- **Method**: `POST`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Response**:
```json
{
    "id": "number",
    "title": "string",
    "url": "string",
    "album": {
        "id": "number",
        "name": "string"
    }
}
```
- **Error Codes**:
  - 401: 未授权
  - 404: 照片不存在
  - 500: 服务器错误

### 4.3 永久删除照片
- **URL**: `/api/trash/{id}`
- **Method**: `DELETE`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Response**:
  - 状态码：204 No Content
- **Error Codes**:
  - 401: 未授权
  - 404: 照片不存在
  - 500: 服务器错误

### 4.4 设置照片删除时间（测试用）
- **URL**: `/api/trash/set-delete-date/{photoId}`
- **Method**: `POST`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Response**:
```json
{
    "message": "Delete date set for photo ID: {photoId}"
}
```
- **Error Codes**:
  - 401: 未授权
  - 404: 照片不存在
  - 500: 服务器错误

### 4.5 手动触发自动删除（测试用）
- **URL**: `/api/trash/auto-delete`
- **Method**: `POST`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Response**:
```json
{
    "message": "Automatic deletion executed."
}
```
- **Error Codes**:
  - 401: 未授权
  - 500: 服务器错误

## 5. 图片编辑模块

### 5.1 获取照片信息
- **URL**: `/api/image-edit/{id}`
- **Method**: `GET`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Response**:
```json
{
    "id": "number",
    "title": "string",
    "url": "string",
    "album": {
        "id": "number",
        "name": "string"
    }
}
```
- **Error Codes**:
  - 401: 未授权
  - 404: 照片不存在
  - 500: 服务器错误

### 5.2 综合编辑照片
- **URL**: `/api/image-edit/edit`
- **Method**: `POST`
- **Headers**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: application/json`
- **Request Body**:
```json
{
    "imageUrl": "string",
    "edits": {
        "brightness": "number",    // 可选，亮度调整（-100到100）
        "contrast": "number",      // 可选，对比度调整（0到2）
        "saturation": "number",    // 可选，饱和度调整（0到2）
        "rotation": "number",      // 可选，旋转角度（0, 90, 180, 270）
        "filter": "string"         // 可选，滤镜类型（grayscale, vintage, warm, cool, dramatic, fade, muted）
    }
}
```
- **Response**:
```json
{
    "editedImageUrl": "string"  // 编辑后的预览图片URL
}
```
- **Error Codes**:
  - 400: 参数错误
  - 401: 未授权
  - 500: 服务器错误

### 5.3 裁剪照片
- **URL**: `/api/image-edit/crop`
- **Method**: `POST`
- **Headers**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: application/json`
- **Request Body**:
```json
{
    "imageUrl": "string",
    "cropData": {
        "x": "number",      // 裁剪起始X坐标
        "y": "number",      // 裁剪起始Y坐标
        "width": "number",  // 裁剪宽度
        "height": "number"  // 裁剪高度
    }
}
```
- **Response**:
```json
{
    "editedImageUrl": "string"  // 裁剪后的预览图片URL
}
```
- **Error Codes**:
  - 400: 参数错误
  - 401: 未授权
  - 500: 服务器错误

### 5.4 保存编辑后的照片
- **URL**: `/api/image-edit/save`
- **Method**: `POST`
- **Headers**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: application/json`
- **Request Body**:
```json
{
    "imageUrl": "string",      // 原始图片URL
    "editedImageUrl": "string" // 编辑后的预览图片URL
}
```
- **Response**:
```json
{
    "savedImageUrl": "string"  // 保存后的最终图片URL
}
```
- **Error Codes**:
  - 400: 参数错误
  - 401: 未授权
  - 500: 服务器错误

### 5.5 应用基本调整
- **URL**: `/api/image-edit/{id}/adjust`
- **Method**: `PUT`
- **Headers**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: application/json`
- **Request Body**:
```json
{
    "brightness": "number",    // 可选，亮度调整（-100到100）
    "contrast": "number",      // 可选，对比度调整（0到2）
    "saturation": "number"     // 可选，饱和度调整（0到2）
}
```
- **Response**:
```json
{
    "id": "number",
    "title": "string",
    "url": "string",      // 调整后的图片URL
    "album": {
        "id": "number",
        "name": "string"
    }
}
```
- **Error Codes**:
  - 400: 参数错误
  - 401: 未授权
  - 404: 照片不存在
  - 500: 服务器错误

### 5.6 应用滤镜
- **URL**: `/api/image-edit/{id}/filter`
- **Method**: `PUT`
- **Headers**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: application/json`
- **Request Body**:
```json
{
    "filter": "string"  // 滤镜类型（grayscale, vintage, warm, cool, dramatic, fade, muted）
}
```
- **Response**:
```json
{
    "id": "number",
    "title": "string",
    "url": "string",      // 应用滤镜后的图片URL
    "album": {
        "id": "number",
        "name": "string"
    }
}
```
- **Error Codes**:
  - 400: 参数错误
  - 401: 未授权
  - 404: 照片不存在
  - 500: 服务器错误

### 5.7 应用变换
- **URL**: `/api/image-edit/{id}/transform`
- **Method**: `PUT`
- **Headers**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: application/json`
- **Request Body**:
```json
{
    "rotation": "number",    // 可选，旋转角度（0, 90, 180, 270）
    "flip": "string"         // 可选，翻转方向（horizontal, vertical）
}
```
- **Response**:
```json
{
    "id": "number",
    "title": "string",
    "url": "string",      // 变换后的图片URL
    "album": {
        "id": "number",
        "name": "string"
    }
}
```
- **Error Codes**:
  - 400: 参数错误
  - 401: 未授权
  - 404: 照片不存在
  - 500: 服务器错误

### 5.8 裁剪照片（直接保存）
- **URL**: `/api/image-edit/{id}/crop`
- **Method**: `PUT`
- **Headers**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: application/json`
- **Request Body**:
```json
{
    "x": "number",      // 裁剪起始X坐标
    "y": "number",      // 裁剪起始Y坐标
    "width": "number",  // 裁剪宽度
    "height": "number"  // 裁剪高度
}
```
- **Response**:
```json
{
    "id": "number",
    "title": "string",
    "url": "string",      // 裁剪后的图片URL
    "album": {
        "id": "number",
        "name": "string"
    }
}
```
- **Error Codes**:
  - 400: 参数错误
  - 401: 未授权
  - 404: 照片不存在
  - 500: 服务器错误 