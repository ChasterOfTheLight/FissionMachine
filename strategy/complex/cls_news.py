import requests
import pandas as pd
from datetime import datetime
import time

def stock_info_cls() -> pd.DataFrame:
    """
    财联社-电报
    https://www.cls.cn/telegraph
    :return: 财联社电报
    :rtype: pandas.DataFrame
    """
    url = "https://www.cls.cn/api/sw"
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36",
        "Accept": "application/json, text/plain, */*",
        "Content-Type": "application/json;charset=UTF-8",
        "Origin": "https://www.cls.cn",
        "Referer": "https://www.cls.cn/telegraph",
        "Cookie": f"VISITID={int(time.time()*1000)}"
    }
    
    params = {
        "app": "CailianpressWeb",
        "os": "web",
        "sv": "7.7.5"
    }
    
    data = {
        "type": "telegram",
        "keyword": "",
        "page": 0,
        "rn": 100,
        "flag": 1,
        "date": datetime.now().strftime("%Y-%m-%d"),  # 添加日期参数
        "search_type": "" 
    }

    try:
        r = requests.post(url, headers=headers, params=params, json=data, timeout=10)
        r.raise_for_status()
        
        data_json = r.json()
        
        # 修改判断逻辑以匹配新的JSON结构
        if (data_json.get("errno") == 0 and "data" in data_json 
            and "telegram" in data_json["data"] 
            and "data" in data_json["data"]["telegram"]):
            
            time_list = []
            text_list = []
            for item in data_json["data"]["telegram"]["data"]:
                # 修改时间戳字段名
                time_str = datetime.fromtimestamp(item.get("time", 0)).strftime('%Y-%m-%d %H:%M:%S')
                time_list.append(time_str)
                # 修改内容字段名
                text_list.append(item.get("descr", ""))
            
            temp_df = pd.DataFrame({
                "时间": time_list,
                "内容": text_list
            })
            return temp_df
        else:
            print("API返回数据格式不正确:")
            print(data_json)
            return pd.DataFrame(columns=["时间", "内容"])
            
    except Exception as e:
        print(f"获取新闻数据失败: {str(e)}")
        return pd.DataFrame(columns=["时间", "内容"])

def get_cls_news():
    news_df = stock_info_cls()
    return news_df

def interpret_news(news_content):
    messages = [{'role': 'system', 'content': '你是一个有股票推荐经验的投资专家。你基于专业的投资知识，一步步的思考，推演并判断每条新闻对该股票的利好程度。'},
                {'role': 'user', 'content': f'请解读以下新闻内容：\n\n{news_content}'}]
    response = Generation.call(api_key='your-key',
                               model="qwen-plus",
                               messages=messages,
                               seed=random.randint(1, 10000),
                               result_format='message')
    if response.status_code == HTTPStatus.OK:
        return response.output.choices[0].message.content
    else:
        print('Request id: %s, Status code: %s, error code: %s, error message: %s' % (
            response.request_id, response.status_code,
            response.code, response.message
        ))

if __name__ == "__main__":
    news_df = get_cls_news()
    if not news_df.empty:
        print("\n=== 财联社最新新闻 ===")
        for index, row in news_df.iterrows():
            print(f"\n{row['时间']}")
            print(f">>> {row['内容']}")
    else:
        print("暂无新闻数据")