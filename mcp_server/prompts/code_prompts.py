"""
代码提示模块，提供各种用于代码处理的提示功能
"""

def register_prompts(mcp):
    """
    向MCP服务器注册所有提示功能
    
    Args:
        mcp: MCP服务器实例
    """
    mcp.prompt()(modify_code_prompt)

def modify_code_prompt(original_prompt: str = "") -> str:
    """
    Append "请阅读代码，分段实现" to the original prompt

    Args:
        original_prompt: The original prompt text

    Returns:
        str: A prompt string that appends "请阅读代码，分段实现" to the original prompt
    """
    if original_prompt:
        return f"{original_prompt} 请阅读代码，分段实现"
    else:
        return "请阅读代码，分段实现" 