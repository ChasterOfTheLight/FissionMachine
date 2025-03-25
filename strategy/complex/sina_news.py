from http import HTTPStatus
from dashscope import Generation
import random
import requests
import pandas as pd

def stock_info_global_sina() -> pd.DataFrame:
    """
    新浪财经-全球财经快讯
    https://finance.sina.com.cn/7x24
    :return: 全球财经快讯
    :rtype: pandas.DataFrame
    """
    url = "https://zhibo.sina.com.cn/api/zhibo/feed"
    params = {
        "page": "1",
        "page_size": "100",
        "zhibo_id": "152",
        "tag_id": "0",
        "dire": "f",
        "dpc": "1",
        "pagesize": "100",
        "type": "1",
    }
    r = requests.get(url, params=params)
    data_json = r.json()
    time_list = [
        item["create_time"] for item in data_json["result"]["data"]["feed"]["list"]
    ]
    text_list = [
        item["rich_text"] for item in data_json["result"]["data"]["feed"]["list"]
    ]
    temp_df = pd.DataFrame([time_list, text_list]).T
    temp_df.columns = ["时间", "内容"]
    return temp_df

def get_sina_news():
    news_df = stock_info_global_sina()
    return news_df

def interpret_news(news_content):
    messages = [{'role': 'system', 'content': '你是一个有股票推荐经验的投资专家。你基于专业的投资知识，一步步的思考，推演并判断每条新闻对该股票的利好程度。'},
                {'role': 'user', 'content': f'请解读以下新闻内容：\n\n{news_content}'}]
    response = Generation.call(api_key='your-key',
                               model="qwen-plus",
                               messages=messages,
                               # 设置随机数种子seed，如果没有设置，则随机数种子默认为1234
                               seed=random.randint(1, 10000),
                               # 将输出设置为"message"格式
                               result_format='message')
    if response.status_code == HTTPStatus.OK:
        return response.output.choices[0].message.content
    else:
        print('Request id: %s, Status code: %s, error code: %s, error message: %s' % (
            response.request_id, response.status_code,
            response.code, response.message
        ))


if __name__ == "__main__":
    news_df = get_sina_news()
    for index, row in news_df.iterrows():
        news_content = row['内容']
        print(f"序号: {index} 新闻内容: {news_content}")
        # interpretation = interpret_news(news_content)
        # print(f"\n解读: {interpretation}\n")