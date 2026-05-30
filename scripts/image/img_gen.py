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
            "text": "电影感组图，记录同一只流浪橘猫，特征必须前后一致。第一张：春天，橘猫穿梭在盛开的樱花树下；第二张：夏天，橘猫在老街的树荫下乘凉避暑；第三张：秋天，橘猫踩在满地的金色落叶上；第四张：冬天，橘猫在雪地上走留下足迹。"
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