from setuptools import setup, find_packages

# 从pyproject.toml读取基本配置
# 仅保留需要动态处理的部分
setup(
    packages=find_packages(),
) 