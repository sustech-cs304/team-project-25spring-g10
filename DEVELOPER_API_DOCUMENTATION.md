# La-Album 开发者API文档

## 概览

La-Album 是一个基于 REST 架构的相册管理系统，采用模块化设计，支持用户认证、相册管理、照片处理和回收站功能。本文档旨在帮助开发者理解系统架构、API设计原理以及如何扩展现有功能。

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

基于实际代码结构，La-Album系统包含以下六个核心模块：

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

#### 扩展指南
- **角色系统**: 在User模型中添加role字段
- **第三方登录**: 集成OAuth2.0服务
- **用户权限**: 实现基于角色的访问控制

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

#### 核心业务逻辑
1. **相册分类系统**:
   - `default`: 普通用户相册
   - `system`: 系统特殊相册（如"全部照片"）

2. **权限控制**:
   - 每个API都验证相册所有权
   - 使用ThreadLocal获取当前用户信息

3. **图片URL处理**:
   - 自动为相册中的照片生成OSS签名URL
   - 确保图片访问的安全性

#### 扩展指南
- **相册共享**: 添加相册共享功能
- **相册标签**: 实现相册分类和标签系统
- **相册模板**: 支持预设相册模板

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

#### 核心业务逻辑
1. **文件上传处理**:
   - 支持JPG、PNG、GIF格式
   - 文件大小限制10MB
   - 文件类型和大小验证
   - OSS云存储集成

2. **图片URL安全机制**:
   - 所有图片URL通过OSS签名URL访问
   - 防止直接访问OSS资源
   - URL有效期控制

3. **搜索功能**:
   - 支持关键词搜索
   - 按日期范围筛选
   - 按相册筛选
   - 分页查询支持

#### 扩展指南
- **批量操作**: 支持批量上传和删除
- **图片元数据**: 提取EXIF信息
- **AI标签**: 自动图像识别和标记
- **缩略图**: 自动生成多尺寸缩略图

### 2.4 记忆视频模块 (Memory Video)

#### 架构设计
- **控制器**: `MemoryController.java`
- **服务层**: `MemoryService.java`
- **数据模型**: `Memory.java`、`MemoryPhoto.java`
- **存储库**: `MemoryRepository.java`

#### 核心数据模型
```java
@Entity
@Table(name = "memories")
public class Memory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;           // 视频标题
    private String thumbnailUrl;    // 视频缩略图URL
    private Long duration;          // 视频时长（秒）
    private String transition;      // 转场效果
    private Long bgmId;             // 背景音乐ID
    private String bgmName;         // 背景音乐名称
    private LocalDateTime createdAt;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;              // 所属用户
    
    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;            // 关联相册
    
    @ElementCollection
    @CollectionTable(name = "memory_photos")
    private List<MemoryPhoto> photos; // 包含的照片及显示时长
}
```

#### API端点实现

| HTTP方法 | 路径 | 功能 | 控制器方法 | 特点 |
|----------|------|------|------------|------|
| GET | `/api/memory` | 获取所有记忆视频 | `getAllMemories()` | 返回扩展信息 |
| GET | `/api/memory/{id}` | 获取记忆视频详情 | `getMemoryById()` | 包含照片列表 |
| POST | `/api/memory/generate` | 生成记忆视频 | `generateMemory()` | 复杂参数处理 |
| PUT | `/api/memory/{id}` | 更新记忆视频 | `updateMemory()` | 权限验证 |
| DELETE | `/api/memory/{id}` | 删除记忆视频 | `deleteMemory()` | 权限验证 |

#### 核心业务逻辑
1. **视频生成流程**:
   - 选择照片和相册
   - 设置转场效果（fade, slide, zoom, rotate, random）
   - 选择背景音乐
   - 配置每张照片显示时长
   - 生成缩略图

2. **数据结构设计**:
   - 主表存储视频基本信息
   - 关联表存储照片列表和显示时长
   - 支持复杂的数据查询和更新

3. **URL处理**:
   - 所有照片和缩略图URL自动签名
   - 确保视频资源的安全访问

#### 扩展指南
- **视频导出**: 支持实际视频文件生成
- **模板系统**: 预设视频模板
- **音乐库**: 扩展背景音乐选择
- **效果库**: 更多转场和特效

### 2.5 图片编辑模块 (Image Editing)

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

#### 核心业务逻辑
1. **两种编辑模式**:
   - **预览模式**: POST接口，生成预览图片，不修改原始数据
   - **直接保存模式**: PUT接口，直接修改数据库中的照片记录

2. **文件处理策略**:
   - 预览图片存储在本地静态文件目录
   - 生成唯一文件名避免冲突
   - 支持preview_和final_文件命名规则

3. **图像处理链**:
   - 支持多种操作的组合应用
   - 按顺序执行：亮度→对比度→饱和度→旋转→滤镜

#### 扩展指南
- **高级编辑**: 添加更多专业图像处理功能
- **历史记录**: 支持编辑历史和撤销功能
- **批量处理**: 支持批量应用编辑操作
- **云端处理**: 集成云端图像处理服务

### 2.6 回收站模块 (Trash Management)

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

#### 核心业务逻辑
1. **软删除机制**:
   - 照片删除时移动到回收站而非直接删除
   - 保留原始相册信息便于恢复
   - 维护用户数据的完整性

2. **恢复机制**:
   - 支持照片恢复到原始相册
   - 如果原始相册被删除，恢复到默认相册
   - 恢复后自动从回收站移除

3. **永久删除**:
   - 从数据库删除记录
   - 同时删除OSS存储的实际文件
   - 不可逆操作，需要确认

#### 扩展指南
- **自动清理**: 定时任务清理过期照片
- **批量操作**: 支持批量恢复和删除
- **回收站统计**: 显示回收站使用情况
- **删除提醒**: 照片即将永久删除的提醒机制

## 3. 错误处理架构

### 3.1 HTTP状态码规范

| 状态码 | 含义 | 使用场景 |
|--------|------|----------|
| 200 | 成功 | 正常响应 |
| 201 | 已创建 | 资源创建成功 |
| 204 | 无内容 | 删除操作成功 |
| 400 | 请求错误 | 参数验证失败 |
| 401 | 未授权 | 认证失败或token过期 |
| 403 | 禁止访问 | 权限不足 |
| 404 | 未找到 | 资源不存在 |
| 409 | 冲突 | 资源已存在 |
| 500 | 服务器错误 | 内部错误 |

### 3.2 错误处理最佳实践

```typescript
interface ApiError {
    code: number;
    message: string;
    details?: any;
    timestamp: Date;
    path: string;
}

class ErrorHandler {
    static handleValidationError(error: ValidationError): ApiError;
    static handleAuthError(error: AuthError): ApiError;
    static handleNotFoundError(resource: string): ApiError;
    static handleServerError(error: Error): ApiError;
}
```

## 4. 认证和授权

### 4.1 JWT实现细节

```typescript
interface JWTPayload {
    userId: number;
    username: string;
    iat: number;  // 签发时间
    exp: number;  // 过期时间
}

class AuthManager {
    generateToken(user: User): string;
    verifyToken(token: string): JWTPayload;
    refreshToken(oldToken: string): string;
}
```

### 4.2 权限检查策略

- **资源所有权**: 用户只能访问自己的相册和照片
- **操作权限**: 不同操作需要不同级别的验证
- **中间件模式**: 使用认证中间件统一处理权限检查

## 5. 数据库设计指南

### 5.1 核心实体关系

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

### 5.2 索引优化建议

```sql
-- 用户查询优化
CREATE INDEX idx_users_username ON users(username);

-- 相册查询优化
CREATE INDEX idx_albums_user_id ON albums(user_id);
CREATE INDEX idx_albums_create_time ON albums(create_time);

-- 照片查询优化
CREATE INDEX idx_photos_album_id ON photos(album_id);
CREATE INDEX idx_photos_create_time ON photos(create_time);

-- 回收站查询优化
CREATE INDEX idx_trash_user_id ON trash_items(user_id);
CREATE INDEX idx_trash_delete_time ON trash_items(delete_time);
```

## 6. 性能优化指南

### 6.1 缓存策略

- **用户会话**: Redis缓存JWT token状态
- **图片URL**: 缓存带签名的URL，减少重复生成
- **相册列表**: 缓存用户相册列表
- **搜索结果**: 缓存热门搜索结果

### 6.2 文件存储优化

- **CDN集成**: 使用CDN加速图片访问
- **压缩策略**: 自动压缩大尺寸图片
- **分布式存储**: 支持多个存储服务提供商
- **预签名URL**: 减少服务器负载

### 6.3 数据库优化

- **连接池**: 配置合适的连接池大小
- **查询优化**: 使用索引优化常用查询
- **分页策略**: 大数据集的分页查询
- **读写分离**: 支持读写分离架构

## 7. 测试策略

### 7.1 单元测试

```typescript
// 用户服务测试示例
describe('UserService', () => {
    test('should create user successfully', async () => {
        const userData = { username: 'testuser', password: 'password123' };
        const result = await userService.register(userData.username, userData.password);
        expect(result).toBeDefined();
    });

    test('should throw error for duplicate username', async () => {
        await expect(userService.register('existinguser', 'password123'))
            .rejects.toThrow('用户名已存在');
    });
});
```

### 7.2 集成测试

```typescript
// API端点测试示例
describe('Album API', () => {
    test('POST /api/albums should create new album', async () => {
        const response = await request(app)
            .post('/api/albums')
            .set('Authorization', `Bearer ${userToken}`)
            .send({ name: 'Test Album', description: 'Test Description' })
            .expect(200);

        expect(response.body.data.name).toBe('Test Album');
    });
});
```

### 7.3 测试工具配置

- **Jest**: 单元测试框架
- **Supertest**: API测试工具
- **Test Containers**: 数据库测试环境
- **Mock服务**: 外部服务模拟

## 8. 部署和运维

### 8.1 环境配置

```typescript
interface AppConfig {
    database: {
        host: string;
        port: number;
        username: string;
        password: string;
        database: string;
    };
    jwt: {
        secret: string;
        expiresIn: string;
    };
    storage: {
        provider: 'aws' | 'aliyun' | 'local';
        bucket: string;
        region: string;
    };
    redis: {
        host: string;
        port: number;
        password?: string;
    };
}
```

### 8.2 监控和日志

- **请求日志**: 记录所有API请求和响应
- **错误监控**: 实时错误报警和追踪
- **性能监控**: API响应时间和数据库查询性能
- **业务指标**: 用户活跃度、上传量等指标

### 8.3 安全建议

- **HTTPS**: 强制使用HTTPS
- **CORS配置**: 正确配置跨域策略
- **输入验证**: 严格验证所有输入参数
- **SQL注入防护**: 使用参数化查询
- **文件上传安全**: 验证文件类型和大小

## 9. 扩展开发指南

### 9.1 添加新的API端点

1. **定义接口**: 在相应的Service接口中添加方法
2. **实现逻辑**: 在Service实现类中编写业务逻辑
3. **创建控制器**: 添加新的控制器方法
4. **配置路由**: 在路由配置中添加新端点
5. **编写测试**: 添加相应的单元测试和集成测试
6. **更新文档**: 更新API文档

### 9.2 添加新的功能模块

1. **设计数据模型**: 定义新的实体和关系
2. **创建服务层**: 实现业务逻辑
3. **添加控制器**: 创建REST端点
4. **数据库迁移**: 创建数据库表和索引
5. **权限控制**: 实现访问控制逻辑
6. **测试覆盖**: 确保充分的测试覆盖

### 9.3 性能优化指南

1. **识别瓶颈**: 使用性能监控工具
2. **数据库优化**: 优化查询和索引
3. **缓存策略**: 实现适当的缓存机制
4. **代码优化**: 优化算法和数据结构
5. **架构优化**: 考虑微服务架构

## 10. 常见问题和解决方案

### 10.1 认证问题

**问题**: JWT token过期处理
**解决方案**: 实现token刷新机制，前端自动处理token续期

### 10.2 文件上传问题

**问题**: 大文件上传超时
**解决方案**: 实现分片上传和断点续传功能

### 10.3 性能问题

**问题**: 大量照片加载缓慢
**解决方案**: 实现懒加载和虚拟滚动，使用缩略图

### 10.4 存储问题

**问题**: 存储成本过高
**解决方案**: 实现图片压缩和冷存储策略

## 总结

La-Album API采用现代化的架构设计，具有良好的扩展性和可维护性。通过模块化设计，开发者可以轻松添加新功能或修改现有功能。遵循本文档的指导原则，可以确保代码质量和系统稳定性。

开发者在扩展系统时，应该：
1. 遵循现有的架构模式
2. 保持API设计的一致性
3. 注重错误处理和用户体验
4. 编写充分的测试
5. 及时更新文档

如有疑问或需要进一步的技术支持，请参考源码注释或联系开发团队。 