# La-Album MCP Server

MCP (Model Control Protocol) 服务器，为Cursor提供La-Album应用的API工具。

## 功能

- 用户认证（登录、注册、获取用户信息等）
- 相册管理（创建、获取、更新、删除相册）
- 代码提示功能

## 结构

项目采用模块化结构：

```
mcp_server/
│
├── main.py                 # 主入口文件
├── config.py               # 配置文件
├── session_manager.py      # 会话管理类
│
├── api/                    # API工具函数目录
│   ├── __init__.py         
│   ├── auth.py             # 用户认证相关API
│   ├── albums.py           # 相册管理相关API
│
├── utils/                  # 工具函数目录
│   ├── __init__.py
│   ├── decorators.py       # 装饰器
│   └── http_utils.py       # HTTP请求工具
│
├── prompts/                # 提示相关功能目录
│   ├── __init__.py
│   └── code_prompts.py     # 代码提示
│
└── tests/                  # 测试目录
```

## 安装

```bash
# 安装依赖
pip install -e .
```

## 使用

```bash
# 运行MCP服务器
python main.py
```

然后在Cursor中连接到MCP服务器，使用提供的工具。

## 开发

可以继续添加更多API模块，如:
- photos.py - 照片管理模块
- trash.py - 回收站管理模块
- image_edit.py - 图片编辑模块
