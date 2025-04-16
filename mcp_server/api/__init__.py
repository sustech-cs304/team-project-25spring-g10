"""
API模块包，包含各种API工具

提供所有与La-Album API交互的工具函数
"""

from . import auth
from . import albums

__all__ = ['auth', 'albums']
