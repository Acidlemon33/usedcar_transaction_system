from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/predict', methods=['POST'])
def predict():
    # 打印 Java 发过来的数据，看看对不对
    print("收到 Java 的请求数据:", request.json)
    # 随便返回一个假价格
    return jsonify({"price": 158000.5})

if __name__ == '__main__':
    app.run(port=8000) # 运行在 8000 端口