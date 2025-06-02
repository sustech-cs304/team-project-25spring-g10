启动 Redis 服务端程序 redis-server.exe redis.conf

修改数据库配置

后端
- cd *kend
- mvn spring-boot:run

前端
- cd *tend
- pnpm run serve

前端测试环境搭建
- cd *tend
- pnpm install @vue/test-utils@next --save-dev  # 安装 Vue Test Utils
- pnpm install jest@latest @babel/preset-env@latest babel-jest@latest --save-dev  # 安装 Jest 和相关依赖
- pnpm install @vue/vue3-jest@latest --save-dev  # 安装 Vue 3 Jest 支持
- pnpm install @babel/core@latest --save-dev  # 安装 Babel 核心
- 在 package.json 中添加测试脚本:
- 运行测试并生成覆盖率报告: npm run test:coverage   

风格迁移 pytorch-neural-style-transfer
- cd pytorch-neural-style-transfer
- 首次 conda env create[根据environment.yml安装依赖]
- conda activate pytorch-nst
- uvicorn main:app --reload --port 8000
    [出现
    INFO:     Will watch for changes in these directories: ['D:\\Album\\pytorch-neural-style-transfer']
    INFO:     Uvicorn running on http://127.0.0.1:8000 (Press CTRL+C to quit)
    INFO:     Started reloader process [25836] using StatReload
    INFO:     Started server process [29824]
    INFO:     Waiting for application startup.
    INFO:     Application startup complete.
    ]

ai
- conda activate faceAI
- cd flaskProject
- python app.py

cloc-2.04.exe . --exclude-dir=node_modules