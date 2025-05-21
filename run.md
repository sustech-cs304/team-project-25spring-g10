
启动 Redis 服务端程序 redis-server.exe redis.conf

修改数据库配置

后端
- cd *kend
- mvn spring-boot:run

前端
- cd *tend
- pnpm run serve

风格迁移
- 首次 conda env create[根据environment.yml安装依赖]
- cd pytorch-neural-style-transfer
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
    