# La-Album 开发者文档

## 1. 系统架构

### 1.1 核心设计原则

- **RESTful API设计**: 遵循REST原则，使用标准HTTP方法（GET、POST、PUT、DELETE、PATCH）
- **JWT认证机制**: 无状态的用户认证，支持分布式部署
- **分层架构**: 控制器层 → 服务层 → 数据访问层
- **模块化设计**: 各功能模块独立，便于维护和扩展

### 1.2 技术栈

- **认证**: JWT (JSON Web Token)
- **数据格式**: JSON
- **文件上传**: multipart/form-data
- **错误处理**: 标准HTTP状态码 + 自定义错误信息

### 1.3 API响应格式标准

所有API响应都遵循统一的格式：

```json
{
    "code": number,      // HTTP状态码
    "message": string,   // 响应消息
    "data": any         // 数据载荷，可为null
}
```

## 2. 核心模块架构

### 2.0 基础说明
### 2.0.1 错误码说明
| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未授权或token无效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 500 | 服务器内部错误 |

### 2.0.2 JWT拦截器权限说明
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
   

### 2.1 用户管理模块 (User Management)

#### 架构设计
- **控制器**: `UserController.java`
- **服务层**: `UserService.java`
- **数据模型**: `User.java`
- **存储库**: `UserRepository.java`

#### 核心数据模型
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String username;        // 用户名（5-16位）
    
    @JsonIgnore
    private String password;        // MD5加密密码
    
    private String email;           // 邮箱（可选）
    private String userPic;         // 头像URL
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Album> albums;     // 用户相册
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Memory> memories;  // 用户记忆视频
}
```

#### API端点实现

| HTTP方法 | 路径 | 功能 | 控制器方法 | 认证要求 |
|----------|------|------|------------|----------|
| GET | `/api/users` | 获取所有用户 | `getAllUsers()` | 是 |
| GET | `/api/users/{id}` | 根据ID获取用户 | `getUserById()` | 是 |
| GET | `/api/users/username/{username}` | 根据用户名获取用户 | `getUserByUsername()` | 是 |
| POST | `/api/users/register` | 用户注册 | `register()` | 否 |
| POST | `/api/users/login` | 用户登录 | `login()` | 否 |
| POST | `/api/users/logout` | 用户登出 | `logout()` | 是 |
| GET | `/api/users/userInfo` | 获取当前用户信息 | `userInfo()` | 是 |
| PUT | `/api/users/update` | 更新用户信息 | `update()` | 是 |
| PATCH | `/api/users/updateAvatar` | 更新用户头像 | `updateAvatar()` | 是 |
| DELETE | `/api/users/{id}` | 删除用户 | `deleteUser()` | 是 |

#### 核心业务逻辑
1. **用户注册流程**:
   - 验证用户名和密码格式（5-16位非空字符）
   - 检查用户名是否已存在
   - 密码MD5加密存储
   - 自动创建默认相册"全部照片"

2. **用户登录流程**:
   - 验证用户名和密码
   - 生成JWT令牌（包含用户ID和用户名）
   - 将令牌存储在Redis中，设置1小时过期
   - 确保用户有默认相册

3. **认证机制**:
   - 使用JWT令牌进行身份验证
   - 令牌信息存储在ThreadLocal中便于获取
   - Redis存储用户会话状态

### 2.2 相册管理模块 (Album Management)

#### 架构设计
- **控制器**: `AlbumController.java`
- **服务层**: `AlbumService.java`
- **数据模型**: `Album.java`
- **存储库**: `AlbumRepository.java`

#### 核心数据模型
```java
@Entity
@Table(name = "albums")
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;           // 相册标题
    private String description;     // 相册描述
    private LocalDateTime createTime;
    private String type;            // 相册类型（default/custom）
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;              // 所属用户
    
    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL)
    private List<Photo> photos;     // 相册中的照片
}
```

#### API端点实现

| HTTP方法 | 路径 | 功能 | 控制器方法 | 权限检查 |
|----------|------|------|------------|----------|
| GET | `/api/albums` | 获取所有相册 | `getAllAlbums()` | 用户自己的相册 |
| GET | `/api/albums/type/{type}` | 按类型获取相册 | `getAllAlbumsByType()` | 用户自己的相册 |
| POST | `/api/albums` | 创建相册 | `createAlbum()` | 认证用户 |
| POST | `/api/albums/type` | 创建系统相册 | `createAlbumByType()` | 认证用户 |
| GET | `/api/albums/{id}` | 获取指定相册 | `getAlbumById()` | 相册所有者 |
| PUT | `/api/albums/{id}` | 更新相册信息 | `updateAlbum()` | 相册所有者 |
| GET | `/api/albums/{albumId}/photos` | 获取相册照片 | `getPhotosInAlbum()` | 相册所有者 |
| DELETE | `/api/albums/{id}` | 删除相册 | `deleteAlbum()` | 相册所有者 |

### 2.3 照片管理模块 (Photo Management)

#### 架构设计
- **控制器**: `PhotoController.java`
- **服务层**: `PhotoService.java`
- **数据模型**: `Photo.java`、`PhotoDTO.java`
- **存储库**: `PhotoRepository.java`

#### 核心数据模型
```java
@Entity
@Table(name = "photos")
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;           // 照片标题
    private String url;             // OSS存储URL
    private String description;     // 照片描述
    private String location;        // 拍摄地点
    private String tags;            // 照片标签
    private LocalDate date;         // 拍摄日期
    private LocalDateTime uploadTime; // 上传时间
    
    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;            // 所属相册
}
```

#### API端点实现

| HTTP方法 | 路径 | 功能 | 控制器方法 | 特殊要求 |
|----------|------|------|------------|----------|
| GET | `/api/photos` | 获取所有照片 | `getAllPhotos()` | 返回DTO，包含签名URL |
| GET | `/api/photos/{id}` | 获取照片详情 | `getPhoto()` | 返回DTO，签名URL |
| POST | `/api/photos/upload` | 上传照片 | `uploadPhoto()` | multipart/form-data |
| POST | `/api/photos/copy` | 复制照片到相册 | `copyPhotoToAlbum()` | 权限验证 |
| DELETE | `/api/photos/{id}` | 删除照片 | `deletePhoto()` | 移至回收站 |
| PUT | `/api/photos/{id}/move` | 移动照片 | `movePhoto()` | 权限验证 |
| GET | `/api/photos/search` | 搜索照片 | `searchPhotos()` | 支持多条件搜索 |
| PUT | `/api/photos/{id}` | 更新照片信息 | `updatePhotoDescriptionAndTags()` | 权限验证 |
| GET | `/api/photos/proxy` | 图片代理访问 | `proxyImage()` | 安全访问 |

### 2.4 图片编辑模块 (Image Editing)

#### 架构设计
- **控制器**: `ImageEditController.java`
- **工具类**: `ImageEditUtils.java`
- **存储配置**: 本地静态文件存储

#### 支持的编辑操作
```java
public class ImageEditUtils {
    // 基础调整
    public static BufferedImage adjustBrightness(BufferedImage image, int brightness);
    public static BufferedImage adjustContrast(BufferedImage image, double contrast);
    public static BufferedImage adjustSaturation(BufferedImage image, float saturation);
    
    // 几何变换
    public static BufferedImage rotateImage(BufferedImage image, int degrees);
    public static BufferedImage cropImage(BufferedImage image, int x, int y, int width, int height);
    
    // 滤镜效果
    public static BufferedImage applyGrayscaleFilter(BufferedImage image);
    public static BufferedImage applySepiaFilter(BufferedImage image);  // vintage
    public static BufferedImage applyWarmFilter(BufferedImage image);
    public static BufferedImage applyCoolFilter(BufferedImage image);
    public static BufferedImage applyDramaticFilter(BufferedImage image);
    public static BufferedImage applyFadeFilter(BufferedImage image);
    public static BufferedImage applyMutedFilter(BufferedImage image);
}
```

#### API端点实现

| HTTP方法 | 路径 | 功能 | 控制器方法 | 特点 |
|----------|------|------|------------|------|
| GET | `/api/image-edit/{id}` | 获取照片信息 | `getPhoto()` | 编辑前准备 |
| POST | `/api/image-edit/edit` | 综合编辑 | `editImage()` | 预览模式，支持多种操作组合 |
| POST | `/api/image-edit/crop` | 裁剪预览 | `cropImage()` | 非破坏性预览 |
| POST | `/api/image-edit/save` | 保存编辑 | `saveImage()` | 生成最终版本 |
| PUT | `/api/image-edit/{id}/adjust` | 基础调整 | `adjustImage()` | 直接保存到数据库 |
| PUT | `/api/image-edit/{id}/filter` | 应用滤镜 | `applyFilter()` | 直接保存到数据库 |
| PUT | `/api/image-edit/{id}/transform` | 几何变换 | `transformImage()` | 直接保存到数据库 |
| PUT | `/api/image-edit/{id}/crop` | 裁剪照片 | `cropImage()` | 直接保存到数据库 |

### 2.5 回收站模块 (Trash Management)

#### 架构设计
- **控制器**: `TrashedPhotoController.java`
- **服务层**: `TrashedPhotoService.java`
- **数据模型**: `TrashedPhoto.java`
- **存储库**: `TrashedPhotoRepository.java`

#### 核心数据模型
```java
@Entity
@Table(name = "trashed_photos")
public class TrashedPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;           // 照片标题
    private String url;             // 照片URL
    private String description;     // 照片描述
    private LocalDateTime deletedAt; // 删除时间
    private Long originalAlbumId;   // 原始相册ID
    private String originalAlbumName; // 原始相册名称
    private Long userId;            // 用户ID
}
```

#### API端点实现

| HTTP方法 | 路径 | 功能 | 控制器方法 | 备注 |
|----------|------|------|------------|------|
| GET | `/api/trash` | 获取回收站照片 | `getTrashPhotos()` | 按用户隔离 |
| POST | `/api/trash/restore/{id}` | 恢复照片 | `restorePhoto()` | 恢复到原相册 |
| DELETE | `/api/trash/{id}` | 永久删除 | `deleteFromTrash()` | 同时删除OSS文件 |

## 3. 前端视图和组件说明

### 3.1 视图（Views）

#### 3.1.1 用户认证相关
- **LoginView.vue**: 用户登录页面
  - 功能：用户登录表单
  - 路由：`/login`
  - 依赖组件：无

- **RegisterView.vue**: 用户注册页面
  - 功能：用户注册表单
  - 路由：`/register`
  - 依赖组件：无

#### 3.1.2 相册管理相关
- **AlbumListView.vue**: 相册列表页面
  - 功能：展示所有相册，支持创建、编辑、删除相册
  - 路由：`/albums`
  - 依赖组件：`album/AlbumCard.vue`

- **AlbumView.vue**: 单个相册详情页面
  - 功能：展示相册内的照片，支持照片管理
  - 路由：`/album/:id`
  - 依赖组件：`photo/PhotoGrid.vue`

#### 3.1.3 照片管理相关
- **PhotoView.vue**: 照片查看页面
  - 功能：展示单张照片详情，支持编辑、删除等操作
  - 路由：`/photo/:id`
  - 依赖组件：`ImageEditor.vue`

- **PhotoUploadEdit.vue**: 照片上传和编辑页面
  - 功能：照片上传、编辑、添加描述等
  - 路由：`/upload`
  - 依赖组件：`upload/UploadForm.vue`, `ImageEditor.vue`

### 3.2 组件（Components）

#### 3.2.1 布局组件（layout/）
- **Header.vue**: 页面头部导航
- **Sidebar.vue**: 侧边栏导航
- **Footer.vue**: 页面底部

#### 3.2.2 相册组件（album/）
- **AlbumCard.vue**: 相册卡片组件
  - 功能：展示相册预览信息
  - 属性：
    - `album`: 相册数据对象
    - `editable`: 是否可编辑

#### 3.2.3 照片组件（photo/）
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

## 4. 数据库设计指南

### 4.1 核心实体关系

```sql
-- 用户表
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(16) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    user_pic VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 相册表
CREATE TABLE albums (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    user_id INTEGER REFERENCES users(id),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 照片表
CREATE TABLE photos (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200),
    url VARCHAR(500) NOT NULL,
    description TEXT,
    album_id INTEGER REFERENCES albums(id),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 回收站表
CREATE TABLE trash_items (
    id SERIAL PRIMARY KEY,
    photo_id INTEGER REFERENCES photos(id),
    user_id INTEGER REFERENCES users(id),
    original_album_id INTEGER REFERENCES albums(id),
    delete_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

