from flask import Flask, Response, request, jsonify
from flask_cors import CORS
from PIL import Image
import matplotlib.pyplot as plt
import io
import sys
from facenet_pytorch import MTCNN, InceptionResnetV1
import torch
from PIL import Image
import io
from sklearn.cluster import KMeans
import numpy as np
import requests  # Add requests for fetching photos
import logging
from sklearn.metrics.pairwise import cosine_similarity
import openai
import requests
import base64
import os
import dashscope
from dashscope import MultiModalConversation
import re

os.environ['OPENAI_API_KEY'] = "sk-f8e74e82a8cc4af682effa097b7f6633"


app = Flask(__name__)
CORS(app)  # 允许所有域名访问

# Initialize MTCNN and InceptionResnetV1
device = torch.device('cuda:0' if torch.cuda.is_available() else 'cpu')
mtcnn = MTCNN(device=device,keep_all=True)
resnet = InceptionResnetV1(pretrained='vggface2').eval().to(device)

@app.route('/')
def hello_world():  # put application's code here
    return 'Hello World!'

# /** 
#      * AI-generated-content 
#      * tool: ChatGPT
#      * version: 40
#      * usage: I used the prompt 把infer_cn.ipynb 中的人脸检测代码写成flask api", and 
#      * directly copy the code from its response 
#      */ 
@app.route('/detect_faces', methods=['Get'])
def detect_faces():
    try:
        if len(Image) != 0:
            for image in Image:
                print('yes')
            # 使用 MTCNN 检测人脸
                boxes, probs = mtcnn.detect(image)
                if boxes is None:
                    return jsonify({'error': 'No faces detected'}), 400

        return jsonify({'faces'}),400

    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/fetch_photos', methods=['GET'])
def fetch_photos():
    try:
        # 设置 Authorization 头
        headers = {
            'Authorization': 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjbGFpbXMiOnsiaWQiOjIsInVzZXJuYW1lIjoieWhqMTExIn0sImV4cCI6MTc0NDY1MDk1OH0.4YfJvZtsY5SaYZVIFh2ylSjwg0fvZMyymoBIM3ehCKI'  # 替换 YOUR_ACCESS_TOKEN 为实际的令牌
        }

        # Fetch photo metadata from the given URL with Authorization header
        response = requests.get('http://localhost:9090/api/albums', headers=headers)
        

        albums = response.json().get('data',[])
        
        photo_album_mapping = []  # 存储照片 id 和 album id 的键值对
        for album in albums:
            album_id = album['id']
            for photo in album.get('photos', []):
                photo_id = photo['id']
                photo_url = photo['url']
                photo_album_mapping.append({
                    'photoId': photo_id,
                    'albumId': album_id,
                    'photoUrl': photo_url
                })


        results = []
        aligned = []
        embeddings = []
        for photo in photo_album_mapping:
            image_url = photo.get('photoUrl')
            # Download the image
            image_response = requests.get(image_url)

            image = Image.open(io.BytesIO(image_response.content)).convert('RGB')

            # Detect faces
            x_aligned, probs = mtcnn(image,return_prob=True)
           
            if x_aligned is not None:
                print('检测到的人脸及其概率: {:8f}'.format(probs))
                aligned.append(x_aligned)
                # names.append(dataset.idx_to_class[y])

        aligned = torch.stack(aligned).to(device)

        embeddings = resnet(aligned).detach().cpu()
        embeddings_np = embeddings.numpy()

        # 设置聚类的类别数量（例如 3 类）
        num_clusters = 5

        # 使用 K-Means 聚类
        kmeans = KMeans(n_clusters=num_clusters, random_state=42)
        kmeans.fit(embeddings_np)

        # 获取每个嵌入向量的聚类标签
        labels = kmeans.labels_

        # 打印聚类结果
        for i, label in enumerate(labels):
            print(f"Embedding {i} belongs to cluster {label}")

        return response.json()
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    

@app.route('/auto_class', methods=['POST'])
def auto_class():
    try:
        data = request.json
        id = data.get('photoId')  # 获取传入的 URL
        token = data.get('token')  # 获取传入的 token
        # 设置 Authorization 头
        headers = {
            'Authorization': token
        }
        type = 'face'
        # TODO：这里可以改成只找type为face的照片
        response = requests.get(f'http://localhost:9090/api/albums/type/{type}', headers=headers)
        print("idis",id)
        # 通过id获取新上传的照片的url
        response2 = requests.get(f'http://localhost:9090/api/photos/{id}', headers=headers)
        data = response2.json()
        url = data.get('url')
        print("url",url)
        
        image_response = requests.get(url)

        image = Image.open(io.BytesIO(image_response.content)).convert('RGB')

        # Detect faces
        x_aligned, probs = mtcnn(image,return_prob=True)
        
        if x_aligned is not None:
            if len(x_aligned )>2:
                print('检测到多个人脸,是一张合照')
                return jsonify({"albumId": -2}), 200
           

        albums = response.json().get('data',[])
        
        photo_album_mapping = []  # 存储照片 id 和 album id 的键值对
        photo_album_mapping.append({
                    'photoId': 0,
                    'albumId': 0,
                    'photoUrl':url
                })
        for album in albums:
            album_id = album['id']
            # if album_id == 16:
            #     continue
            for photo in album.get('photos', []):
                photo_id = photo['id']
                photo_url = photo['url']
                photo_album_mapping.append({
                    'photoId': photo_id,
                    'albumId': album_id,
                    'photoUrl': photo_url
                })
        
        if(len(photo_album_mapping)<=1):
            print ('没有其他人脸照片')
            return jsonify({"albumId": 0}), 200

        results = []
        aligned = []
        embeddings = []
        
        # 获取传入图片的url
        for photo in photo_album_mapping:
            image_url = photo.get('photoUrl')
            # Download the image
            image_response = requests.get(image_url)

            image = Image.open(io.BytesIO(image_response.content)).convert('RGB')

            # Detect faces
            x_aligned, probs = mtcnn(image,return_prob=True)
           
            if x_aligned is not None:
                if len(x_aligned )>2:
                    continue
                else:
                    aligned.append(x_aligned[0])
            else:
                #未检测出人脸
                print('未检测出人脸')
                return jsonify({"albumId": -1}), 200
                # names.append(dataset.idx_to_class[y])

        aligned = torch.stack(aligned).to(device)

        embeddings = resnet(aligned).detach().cpu()

        last_embedding = embeddings[0].reshape(1,-1)

        # dists = [(last_embedding - e).norm().item() for e in embeddings[:-1]]

        # 计算相似度（余弦相似度，值越大越相似）
        similarities = cosine_similarity(embeddings[1:], last_embedding).flatten()

        index = np.argmax(similarities)
        print(similarities[index])
        if similarities[index]<0.5:
            print('没有找到相似的人脸')
            return jsonify({"albumId": 0}), 200
        
        index=index+1
        album_id = photo_album_mapping[index]['albumId']

        print(album_id)

        return jsonify({"albumId": album_id}), 200

    except Exception as e:
        print(e)
        return jsonify({'error': str(e)}), 500
    
def extract_content_from_braces(response):
    if response.get("status_code") == 200 and response.get("output") and response["output"].get("choices"):
        content = response["output"]["choices"][0]["message"]["content"][0]["text"]
        
        print(content)
        # 使用正则表达式提取花括号中的内容
        desc_match = re.search(r"描述：\[(.*?)\]", content)
        class_match = re.search(r"分类：\[(.*?)\]", content)
        
        description = desc_match.group(1) if desc_match else ""
        classification = class_match.group(1) if class_match else ""
        
        return {
            "description": description,
            "classification": classification
        }
    else:
        return {
            "description": "",
            "classification": "",
            "error": "无法从响应中提取信息"
        }

@app.route('/description', methods=['POST'])
def classify_and_describe_image():
    try:
        data = request.json
        id = data.get('photoId')  # 获取传入的 URL
        token = data.get('token')  # 获取传入的 token
        # 设置 Authorization 头
        headers = {
            'Authorization': token
        }
        response2 = requests.get(f'http://localhost:9090/api/photos/{id}', headers=headers)
        data = response2.json()
        image_url = data.get('url')
        print("url",image_url )
        # 交给千问处理
        dashscope.api_key = os.environ['OPENAI_API_KEY']

        image_data = requests.get(image_url).content
        image_base64 = base64.b64encode(image_data).decode('utf-8')
        image_b64_str = f"data:image/jpeg;base64,{image_base64}"


        # 发送请求到 Qwen-VL
        response = MultiModalConversation.call(
            model='qwen-vl-plus',
            messages=[
                {
                    "role": "user",
                    "content": [
                        {
                            "image": image_b64_str,
                            "type": "image"
                        },
                        {
                            "text": '''请对这张图片进行内容识别，并按以下格式回复：
                            描述:[详细描述图片内容]
                            分类:[选择以下之一：人物、动物、风景、建筑、美食、植物、交通工具、科技产品、文本图、艺术图、卡通、活动、其他],用[]来包裹分类结果

                            example: 描述：[这是一张彭于晏的照片，他穿着白色衬衫，微笑着站在户外]
                            分类：[人物]
                            ''',
                            "type": "text"
                        }
                    ]
                }
            ]
        )
        print(response)
        result = extract_content_from_braces(response)
        return jsonify({
            "description": result['description'],
            "classification": result['classification']
        }), 200

    except Exception as e:
        print(e)
        return jsonify({'error': str(e)}), 500
    



if __name__ == '__main__':
    app.run(host="0.0.0.0", port=7777, debug=True, threaded=True)