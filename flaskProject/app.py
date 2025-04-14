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

app = Flask(__name__)
CORS(app)  # 允许所有域名访问

# Initialize MTCNN and InceptionResnetV1
device = torch.device('cuda:0' if torch.cuda.is_available() else 'cpu')
mtcnn = MTCNN(device=device)
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
            'Authorization': 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjbGFpbXMiOnsiaWQiOjIsInVzZXJuYW1lIjoieWhqMTExIn0sImV4cCI6MTc0NDYzMzU3Nn0.aU1ybZKfleEczrjovloLZ802ZvTxDYS0RUwA8bGlTs4'  # 替换 YOUR_ACCESS_TOKEN 为实际的令牌
        }

        # Fetch photo metadata from the given URL with Authorization header
        response = requests.get('http://localhost:9090/api/photo', headers=headers)
        

        photos = response.json()

        results = []
        aligned = []
        embeddings = []
        for photo in photos:
            image_url = photo.get('url')
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

        return photos
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=7777, debug=True, threaded=True)