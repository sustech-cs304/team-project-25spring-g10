# [CS304] Team Project - Sprint 2 Report

## Part I.  Metrics (1 point)

- **Lines of Code**
- **Number of source files**
- **Cyclomatic complexity**
- **Number of dependencies**

## Part II. Documentation (2 Points)

### ✅ 用户文档（End User Documentation）

我们提供了完整的用户文档 [End User Documentation](docs\End_User_Documentation.md)，包含：

- 注册/登录流程
- 上传照片与智能分类说明
- 相册故事视频生成使用指南
- 协作相册设置方式
- 社交分享与平台推荐操作说明

示意截图如下：  
![上传照片](docs/img/upload.png)  
![相册分类](docs/img/ai_classify.png)

#### ✅ 开发者文档（Developer Documentation）

我们维护了一个开发者文档，详见：[Developer Guide](docs\Developer_Guide.md)

内容包括：

- 项目结构概览
- 数据模型设计（ER图）
- 后端接口文档（SpringBoot REST API）
- AI 分类模块说明（调用流程 + 标签处理）
- 构建 & 部署说明（Dockerfile + CI/CD）

关键文件路径：

- `backend/src/main/java/com/album/controller/`：控制器层  
- `frontend/src/components/`：Vue 组件  
- `docker-compose.yml`：部署配置

## Part III.  Tests (2 Points)

后端使用JUnit 5和Mockito 进行测试, 使用Jacoco生成测试覆盖报告。  
![alt text](tests.png)
前端使用Jest进行测试。
![alt text](frontend-test.png)

## Part IIII.  Build and Deploy(2 Points)

Use github actions and docker to build and deploy.  
![alt text](actions.png)
![alt text](build-success.png)
