import openai
import base64
from openai import OpenAI


client = OpenAI(
    api_key='sk-cm360514929e86e85000259d886b22e3882cd22d871lwNgm',  
    base_url='https://api.gptsapi.net/v1'#可根据镜像站修改
)

#图片转base64函数
def encode_image(image_path):
  with open(image_path, "rb") as image_file:
    return base64.b64encode(image_file.read()).decode('utf-8')
 
#输入图片路径
image_path = "D:/courses/software/team-project-25spring-g10/AI/image.png"
 
#原图片转base64
base64_image = encode_image(image_path)


#提交信息至GPT4o
response = client.chat.completions.create(
    model="gpt-4o",#选择模型
    messages=[
        {
            "role": "system",
            "content": "You are a smart album."
        },
        {
            "role": "user",
            "content":[
            {
          "type": "text",
          "text": "Please categorize this picture. The available categories are food, scenery, people, and architecture"
        },
                    {
          "type": "image_url",
          "image_url":{
            "url": f"data:image/jpeg;base64,{base64_image}"
          }
        },
        ]
        }
    ],
    stream=True,
)

reply = ""
for res in response:
    content = res.choices[0].delta.content
    if content:
        reply += content
        print(content)

print('reply:',reply)