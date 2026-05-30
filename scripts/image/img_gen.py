import os
import dashscope
from dashscope.aigc.image_generation import ImageGeneration
from dashscope.api_entities.dashscope_response import Message

dashscope.base_http_api_url = 'https://dashscope.aliyuncs.com/api/v1'

api_key = os.getenv("DASHSCOPE_API_KEY")

message = Message(
    role="user",
    content=[
        {
            "text": """狗
- 通插画风格（Cartoon Illustration Style）
- 简约线条：轮廓使用简单且流畅的线条，没有复杂的细节。
- 柔和色彩：通常采用柔和的色调和有限的调色板，给人以温暖和愉悦的感觉。
- 平面风格
- 非常适合用作游戏设计
- 纯白色背景（#ffffff），不要有任何乱七八糟的东西
- 正视图
- 全身图,要完整，全部在画中
- 是一个连通图，不能有几个分离的部分
- 没有阴影
- 画风要精致
- 可爱的风格
"""
        }
    ]
)

print("----sync call, please wait a moment----")
rsp = ImageGeneration.call(
        model='wan2.7-image',
        api_key=api_key,
        messages=[message],
        enable_sequential=True,
        n=4,
        size="2K"
    )

print(rsp)