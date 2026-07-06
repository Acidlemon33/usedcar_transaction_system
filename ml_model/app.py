"""
二手车价格预测 API (Flask)
环境依赖：flask, pandas, numpy, joblib, pydantic
"""
from flask import Flask, request, jsonify
from pydantic import BaseModel, field_validator
import joblib
from datetime import datetime
import re
import pandas as pd
import numpy as np
import traceback
import os

app = Flask(__name__)

@app.after_request
def add_cors_headers(response):
    response.headers["Access-Control-Allow-Origin"] = "*"
    response.headers["Access-Control-Allow-Headers"] = "*"
    response.headers["Access-Control-Allow-Methods"] = "GET, POST, PUT, DELETE, OPTIONS"
    return response


def load_model_components():
    try:
        current_dir = os.path.dirname(os.path.abspath(__file__))
        model_path = os.path.join(current_dir, "LightGBM_model.pkl")
        category_maps_path = os.path.join(current_dir, "category_maps.pkl")

        model = joblib.load(model_path)
        category_maps = joblib.load(category_maps_path)

        model_feature_names = getattr(model, "feature_name_", None)
        if model_feature_names is None:
            model_feature_names = getattr(model, "feature_names_", None)

        model_cat_features = None
        if hasattr(model, "cat_feature_names_"):
            model_cat_features = model.cat_feature_names_
        elif hasattr(model, "_categorical_feature_indices") and model_feature_names:
            model_cat_features = [model_feature_names[i] for i in model._categorical_feature_indices]

        print(f"模型路径: {model_path}")
        print("模型加载成功！")
        print(f"模型特征总数: {len(model_feature_names) if model_feature_names else 0}")
        print(f"模型分类特征: {model_cat_features}")
        print(f"分类映射加载成功！包含 {len(category_maps)} 个分类特征")

        return model, model_feature_names, model_cat_features, category_maps
    except Exception as e:
        print(f"模型加载失败: {e}")
        return None, None, None, None


model, model_feature_names, model_cat_features, category_maps = load_model_components()


class CarFeatures(BaseModel):
    brand: str = ""
    model: str = ""
    model_year: int | None = None
    milage: int | float | None = None
    fuel_type: str = ""
    accident: int | None = None
    clean_title: int | None = None
    engine_hp: int | str | None = None
    engine_liters: float | str | None = None
    engine_type: str = ""
    trans_type: str = ""
    ext_color_simple: str = ""
    int_color_simple: str = ""

    @field_validator("engine_hp", mode="before")
    @classmethod
    def parse_hp(cls, v):
        if v is None:
            return 0
        if isinstance(v, str):
            nums = re.findall(r"\d+", v)
            return int(nums[0]) if nums else 0
        return int(v)

    @field_validator("engine_liters", mode="before")
    @classmethod
    def parse_liters(cls, v):
        if v is None:
            return 0.0
        if isinstance(v, str):
            nums = re.findall(r"\d+\.?\d*", v)
            return float(nums[0]) if nums else 0.0
        return float(v)


def predict_price_logic(item: CarFeatures):
    """核心预测逻辑（供 API 调用）"""
    if model is None:
        return {"code": 500, "message": "模型未加载"}

    try:
        current_year = datetime.now().year
        model_year = item.model_year if item.model_year is not None else current_year
        milage = int(item.milage) if item.milage is not None else 0
        car_age = max(0, current_year - model_year)
        mileage_per_year = round(milage / car_age, 2) if car_age > 0 else 0
        brand_model = f"{item.brand}_{item.model}"

        input_data = {
            "brand": item.brand or "",
            "model": item.model or "",
            "model_year": model_year,
            "milage": milage,
            "fuel_type": item.fuel_type or "Unknown",
            "accident": item.accident if item.accident is not None else 0,
            "clean_title": item.clean_title if item.clean_title is not None else 1,
            "engine_hp": item.engine_hp if item.engine_hp is not None else 0,
            "engine_liters": item.engine_liters if item.engine_liters is not None else 0.0,
            "engine_type": item.engine_type or "Unknown",
            "trans_type": item.trans_type or "Unknown",
            "ext_color_simple": item.ext_color_simple or "Other",
            "int_color_simple": item.int_color_simple or "Other",
            "car_age": car_age,
            "mileage_per_year": mileage_per_year,
            "brand_model": brand_model,
        }

        input_df = pd.DataFrame([input_data])

        if category_maps:
            for col, category_list in category_maps.items():
                if col in input_df.columns:
                    value = input_df[col].iloc[0]
                    try:
                        encoded_value = category_list.index(value) if isinstance(category_list, list) else value
                        input_df[col] = encoded_value
                    except (ValueError, AttributeError):
                        print(f"警告: '{col}' 的值 '{value}' 未在映射中找到，使用默认值 0")
                        input_df[col] = 0

        if model_feature_names:
            input_df = input_df.reindex(columns=model_feature_names, fill_value=0)

        input_df = input_df.astype(float)
        pred_log = model.booster_.predict(input_df.values)[0]
        pred_price = np.expm1(pred_log)

        return {"price": int(pred_price)}

    except Exception as e:
        print("错误详情：")
        traceback.print_exc()
        return {"code": 500, "message": f"预测异常: {str(e)}"}


@app.route("/predict", methods=["POST"])
def predict():
    """价格预测接口（POST JSON）"""
    try:
        data = request.get_json(force=True)
        if data is None:
            return jsonify({"code": 400, "message": "请求体必须为 JSON 格式"}), 400

        # 使用 Pydantic 校验输入
        item = CarFeatures(**data)
        result = predict_price_logic(item)
        return jsonify(result)

    except Exception as e:
        print("请求解析失败：")
        traceback.print_exc()
        return jsonify({"code": 400, "message": f"请求参数错误: {str(e)}"}), 400


@app.route("/health", methods=["GET"])
def health():
    """健康检查接口"""
    return jsonify({"status": "ok", "model_loaded": model is not None})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000, debug=False)