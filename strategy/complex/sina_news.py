import akshare as ak
from http import HTTPStatus
from dashscope import Generation
import random

def get_sina_news():
    news_df = ak.stock_info_global_sina()
    return news_df

def interpret_news(news_content):
    messages = [{'role': 'system', 'content': '你是一个有股票推荐经验的投资专家。你基于专业的投资知识，一步步的思考，推演并判断每条新闻对该股票的利好程度。'},
                {'role': 'user', 'content': f'请解读以下新闻内容：\n\n{news_content}'}]
    response = Generation.call(api_key='sk-b3d58dfb1c764cc5a84a0d75ca8f5a8a',
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
        interpretation = interpret_news(news_content)
        print(f"新闻内容: {news_content}\n解读: {interpretation}\n")