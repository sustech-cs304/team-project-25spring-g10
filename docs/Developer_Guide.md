# La-Album 开发者 文档

## 0. 通用说明

### 0.1 错误码说明
| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未授权或token无效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 500 | 服务器内部错误 |

### 0.2 JWT拦截器权限说明
所有需要认证的接口都需要在请求头中携带JWT token，格式为：
```
Authorization: Bearer {token}
```

JWT token包含以下信息：
- 用户ID
- 用户名
- 过期时间（默认24小时）

权限验证规则：
1. 所有以 `/api` 开头的接口都需要进行JWT验证
2. 以下接口除外（无需验证）：
   - `/api/users/register`
   - `/api/users/login`
3. token验证失败时返回401错误码
4. token过期时返回401错误码，需要重新登录

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

### 5.5 基本调整
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

### 5.6 滤镜
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

## 6. 前端视图和组件说明

### 6.1 视图（Views）

#### 6.1.1 用户认证相关
- **LoginView.vue**: 用户登录页面
  - 功能：用户登录表单
  - 路由：`/login`
  - 依赖组件：无

- **RegisterView.vue**: 用户注册页面
  - 功能：用户注册表单
  - 路由：`/register`
  - 依赖组件：无

#### 6.1.2 相册管理相关
- **AlbumListView.vue**: 相册列表页面
  - 功能：展示所有相册，支持创建、编辑、删除相册
  - 路由：`/albums`
  - 依赖组件：`album/AlbumCard.vue`

- **AlbumView.vue**: 单个相册详情页面
  - 功能：展示相册内的照片，支持照片管理
  - 路由：`/album/:id`
  - 依赖组件：`photo/PhotoGrid.vue`

#### 6.1.3 照片管理相关
- **PhotoView.vue**: 照片查看页面
  - 功能：展示单张照片详情，支持编辑、删除等操作
  - 路由：`/photo/:id`
  - 依赖组件：`ImageEditor.vue`

- **PhotoUploadEdit.vue**: 照片上传和编辑页面
  - 功能：照片上传、编辑、添加描述等
  - 路由：`/upload`
  - 依赖组件：`upload/UploadForm.vue`, `ImageEditor.vue`

- **EditPhoto.vue**: 照片编辑页面
  - 功能：照片编辑、滤镜应用、裁剪等
  - 路由：`/edit/:id`
  - 依赖组件：`ImageEditor.vue`

#### 6.1.4 记忆管理相关
- **MemoryListView.vue**: 记忆列表页面
  - 功能：展示所有记忆，支持创建、编辑、删除记忆
  - 路由：`/memories`
  - 依赖组件：`memory/MemoryCard.vue`

- **MemoryView.vue**: 记忆详情页面
  - 功能：展示记忆详情，包含照片和描述
  - 路由：`/memory/:id`
  - 依赖组件：`photo/PhotoGrid.vue`

- **EditMemoryView.vue**: 记忆编辑页面
  - 功能：编辑记忆内容，添加/删除照片
  - 路由：`/memory/edit/:id`
  - 依赖组件：`memory/MemoryForm.vue`

#### 6.1.5 其他功能页面
- **SearchView.vue**: 搜索页面
  - 功能：搜索照片和相册
  - 路由：`/search`
  - 依赖组件：`photo/PhotoGrid.vue`

- **TrashBin.vue**: 回收站页面
  - 功能：管理已删除的照片
  - 路由：`/trash`
  - 依赖组件：`photo/PhotoGrid.vue`

- **StyleTransferPage.vue**: 风格迁移页面
  - 功能：照片风格迁移
  - 路由：`/style-transfer`
  - 依赖组件：`ImageEditor.vue`

- **SharePhoto.vue**: 照片分享页面
  - 功能：生成照片分享链接
  - 路由：`/share/:id`
  - 依赖组件：无

### 6.2 组件（Components）

#### 6.2.1 布局组件（layout/）
- **Header.vue**: 页面头部导航
- **Sidebar.vue**: 侧边栏导航
- **Footer.vue**: 页面底部

#### 6.2.2 相册组件（album/）
- **AlbumCard.vue**: 相册卡片组件
  - 功能：展示相册预览信息
  - 属性：
    - `album`: 相册数据对象
    - `editable`: 是否可编辑

#### 6.2.3 照片组件（photo/）
- **PhotoGrid.vue**: 照片网格组件
  - 功能：网格形式展示照片
  - 属性：
    - `photos`: 照片数组
    - `columns`: 列数
    - `selectable`: 是否可选择

- **PhotoCard.vue**: 照片卡片组件
  - 功能：展示单张照片预览
  - 属性：
    - `photo`: 照片数据对象
    - `selectable`: 是否可选择

#### 6.2.4 记忆组件（memory/）
- **MemoryCard.vue**: 记忆卡片组件
  - 功能：展示记忆预览
  - 属性：
    - `memory`: 记忆数据对象
    - `editable`: 是否可编辑

- **MemoryForm.vue**: 记忆表单组件
  - 功能：创建/编辑记忆
  - 属性：
    - `memory`: 记忆数据对象（可选）
    - `mode`: 模式（create/edit）

#### 6.2.5 上传组件（upload/）
- **UploadForm.vue**: 上传表单组件
  - 功能：处理照片上传
  - 属性：
    - `albumId`: 目标相册ID
    - `multiple`: 是否支持多文件上传

#### 6.2.6 UI组件（ui/）
- **Button.vue**: 自定义按钮组件
- **Input.vue**: 自定义输入框组件
- **Modal.vue**: 模态框组件
- **Loading.vue**: 加载状态组件
- **Toast.vue**: 提示消息组件

### 6.3 组件通信
1. **Props传递**：父组件向子组件传递数据
2. **事件发射**：子组件通过`$emit`向父组件发送事件
3. **Vuex状态管理**：
   - `user`: 用户信息
   - `albums`: 相册列表
   - `photos`: 照片列表
   - `memories`: 记忆列表
   - `ui`: UI状态（加载、提示等）

### 6.4 路由配置
所有路由都在`router/index.js`中配置，主要路由包括：
- `/`: 首页
- `/login`: 登录
- `/register`: 注册
- `/albums`: 相册列表
- `/album/:id`: 相册详情
- `/photo/:id`: 照片详情
- `/upload`: 照片上传
- `/edit/:id`: 照片编辑
- `/memories`: 记忆列表
- `/memory/:id`: 记忆详情
- `/memory/edit/:id`: 记忆编辑
- `/search`: 搜索
- `/trash`: 回收站
- `/style-transfer`: 风格迁移
- `/share/:id`: 照片分享 

### 6.5 前端配置说明

#### 6.5.1 环境变量配置
在项目根目录创建以下环境变量文件：

1. **开发环境** (.env.development)
```
VUE_APP_BASE_API=http://localhost:8080/api
VUE_APP_UPLOAD_URL=http://localhost:8080/api/upload
VUE_APP_STYLE_TRANSFER_URL=http://localhost:8080/api/style-transfer
```


#### 6.5.2 API配置
在`src/api/config.js`中配置API请求：

```javascript
import axios from 'axios'

// 创建axios实例
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      // 处理错误
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    // 处理错误
    return Promise.reject(error)
  }
)

export default service
```


#### 6.5.5 请求工具
在`src/utils\request`目录下配置常用工具函数：

主要功能说明：
1. **基础配置**
   - 设置基础URL为 `http://localhost:9090/api`
   - 设置请求超时时间为15秒

2. **请求拦截器**
   - 自动从 localStorage 获取 token
   - 将 token 添加到请求头中
   - 添加请求调试日志

3. **响应拦截器**
   - 处理响应数据格式
   - 处理业务错误码（code）
   - 处理 HTTP 状态码错误
   - 统一的错误提示
   - 自动处理登录失效情况

4. **错误处理**
   - 401：未授权，自动跳转登录页
   - 403：权限不足
   - 404：接口不存在
   - 500：服务器错误
   - 网络错误处理
   - 请求配置错误处理





