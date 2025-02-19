import akshare as ak
import pandas as pd
import logging
import schedule
import time
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import traceback

# 配置日志记录
logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(message)s", datefmt="%Y-%m-%d %H:%M:%S"
)

# 邮件发送者和接收者
sender_email = "562157205@qq.com"
receiver_email = "zhuifeng_668@qq.com"
password = "tlbvoblptlrwbdfi"  # QQ邮箱的授权码

# 全局变量存储主力净流入数据
global_fund_flow_data = pd.DataFrame()  # 改为空DataFrame而不是字典


def stock_fund_flow_individual():
    global global_fund_flow_data
    three_day_ranking = ak.stock_fund_flow_individual("3日排行")
    # 股票代码不足6位的补0
    three_day_ranking["股票代码"] = three_day_ranking["股票代码"].apply(lambda x: str(x).zfill(6))
    # 股票代码转换为字符串
    three_day_ranking["股票代码"] = three_day_ranking["股票代码"].apply(str)
    # 资金流入净额转换为float数字
    three_day_ranking["资金流入净额"] = three_day_ranking["资金流入净额"].apply(
        convert_to_number)
    # 以资金流入净额为排序依据，降序排列
    global_fund_flow_data = three_day_ranking.sort_values(
        by="资金流入净额", ascending=False)
    return global_fund_flow_data


def convert_to_number(value):
    try:
        if '亿' in str(value):
            return float(str(value).replace('亿', '')) * 10000
        elif '万' in str(value):
            return float(str(value).replace('万', ''))
        return float(value)
    except:
        return 0


def stock_recommendation_strategy(stock_data):
    # 计算移动平均线
    stock_data["MA5"] = stock_data["收盘"].rolling(window=5).mean()
    stock_data["MA10"] = stock_data["收盘"].rolling(window=10).mean()

    # 因子1：收盘价在5日线上
    stock_data["Factor1"] = (stock_data["收盘"] > stock_data["MA5"]).astype(int)

    # 因子2：5日线上穿10日线
    stock_data["Factor2"] = (
        stock_data["MA5"] > stock_data["MA10"]).astype(int)

    # 因子3：最近3天的成交量大于前10天的平均成交量
    stock_data["Volume_MA10"] = stock_data["成交量"].rolling(window=10).mean()
    stock_data["Factor3"] = (stock_data["成交量"].rolling(
        window=3).mean() > stock_data["Volume_MA10"]).astype(int)

    global global_fund_flow_data

    try:
        if not global_fund_flow_data.empty:
            # 获取并格式化股票代码
            stock_code = str(stock_data["股票代码"].iloc[0])

            # 检查股票代码是否存在
            matching_stocks = global_fund_flow_data[global_fund_flow_data["股票代码"] == stock_code]
            
            if not matching_stocks.empty:
                net_inflow = matching_stocks["资金流入净额"].values[0]
                stock_data.loc[:, "主力净流入"] = net_inflow
            else:
                logging.info(f"未找到股票 {stock_code} 的主力净流入数据")
        else:
            logging.warning("global_fund_flow_data 是空的")
    except Exception as e:
        logging.error(f"处理主力净流入数据时出错: {e}")
        logging.error(traceback.format_exc())

    # 因子4：3日主力净流入为正
    stock_data["Factor4"] = (stock_data["主力净流入"] > 0).astype(int)

    # 计算综合评分（调整权重）
    stock_data["Score"] = (
        0.3 * stock_data["Factor1"] +  # 因子1，权重30%
        0.3 * stock_data["Factor2"] +  # 因子2，权重30%
        0.2 * stock_data["Factor3"] +  # 因子3，权重20%
        0.2 * stock_data["Factor4"]  # 因子4，权重20%
    )

    recommendations = stock_data.copy()
    # 按日期降序排列
    recommendations = recommendations.sort_values(by="日期", ascending=False)
    # 取出最新的一条数据
    latest_data = recommendations.iloc[0]
    return latest_data


def get_stock_data(symbol, start_date, end_date):
    return ak.stock_zh_a_hist(
        symbol=symbol,
        period="daily",
        start_date=start_date,
        end_date=end_date,
        adjust="qfq",
        timeout=10,
    )


def get_stock_info(symbol):
    stock_info = ak.stock_individual_info_em(symbol=symbol)
    return stock_info


def filter_stocks():
    stock_info = ak.stock_zh_a_spot_em()
    # 过滤创业板、科创板股票
    stock_info = stock_info[
        ~stock_info["代码"].str.startswith(("3", "4", "8", "9", "68", "bj"))
    ]
    # 过滤新股，次新股
    stock_info = stock_info[~stock_info["代码"].str.startswith(("N", "C"))]
    # 过滤ST股票
    stock_info = stock_info[~stock_info["名称"].str.contains("ST")]
    # 过滤退市股票
    stock_info = stock_info[~stock_info["名称"].str.contains("退市")]
    # 过滤停牌股票（成交量为0的股票可能是停牌股票）
    stock_info = stock_info[stock_info["成交量"] != 0]
    return stock_info["代码"].tolist()


def send_email(subject, body):
    message = MIMEMultipart()
    message["From"] = sender_email
    message["To"] = receiver_email
    message["Subject"] = subject
    message.attach(MIMEText(body, "plain"))

    try:
        server = smtplib.SMTP("smtp.qq.com", 587)
        server.starttls()
        server.login(sender_email, password)
        server.sendmail(sender_email, receiver_email, message.as_string())
        logging.info("邮件发送成功")
    except Exception as e:
        logging.error(f"邮件发送失败: {e}")
    finally:
        server.quit()


def job():
    try:
        logging.info("开始获取股票数据并推荐股票")
        stock_fund_flow_individual()
        stock_list = filter_stocks()
        logging.info(f"共获取到{len(stock_list)}只股票数据")
        all_recommendations = pd.DataFrame(columns=["股票代码", "依据日期", "评分"])
        recommended_stocks = set()
        for stock in stock_list:
            logging.info(f"stockName: {stock}" +
                         f"  index: {stock_list.index(stock)}")
            # 将开始日期提前10天
            stock_data = get_stock_data(stock, "20241223", "20250218")
            # 数据不为空
            if stock_data.empty:
                continue
            stock_recommendation = stock_recommendation_strategy(stock_data)
            new_recommendation = pd.DataFrame(
                {
                    "股票代码": [stock_recommendation["股票代码"]],
                    "依据日期": [stock_recommendation["日期"]],
                    "评分": [stock_recommendation["Score"]],
                }
            )
            # 排除空或全为NA的条目
            if not new_recommendation.isna().all(axis=1).any():
                all_recommendations = pd.concat(
                    [all_recommendations, new_recommendation], ignore_index=True
                )
            recommended_stocks.add(stock_recommendation["股票代码"])
            # 暂停200ms
            time.sleep(0.2)
            # 筛选score > 0.8的股票
            all_recommendations = all_recommendations[all_recommendations["评分"] > 0.8]
            # 打印目前的推荐股票数量
            logging.info(len(all_recommendations))
            # 如果all_recommendations已经有了20条，结束
            if len(all_recommendations) >= 20:
                break
        if all_recommendations.empty:
            logging.info("没有推荐的股票")
        else:
            # 按评分排序并取前十
            all_recommendations["评分"] = pd.to_numeric(
                all_recommendations["评分"], errors="coerce"
            )
            top_recommendations = all_recommendations
            if not top_recommendations.empty:
                body = top_recommendations.to_string(index=False)
                # 邮件标题带上当前日期
                subject = f"股票推荐 {time.strftime('%Y-%m-%d', time.localtime())}"
                # send_email(subject, body)
                logging.info(body)
            else:
                logging.info("没有推荐的股票")
    except Exception as e:
        logging.error(f"任务失败: {e}")
        logging.error(traceback.format_exc())


def job2():
    logging.info("11223344")


# 每个交易日前半小时执行一次job函数
schedule.every().monday.at("09:00").do(job)
schedule.every().tuesday.at("09:00").do(job)
schedule.every().wednesday.at("09:00").do(job)
schedule.every().thursday.at("09:00").do(job)
schedule.every().friday.at("09:00").do(job)

# while True:
#     schedule.run_pending()
#     time.sleep(1)
