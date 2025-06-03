import akshare as ak
import pandas as pd
import logging
from datetime import datetime
import time

# 添加 format_result 函数定义
def format_result(stock_code, stock_name, total_mv, result):
    """格式化股票分析结果"""
    volume_increase = ((result["Volume_MA1"] - result["Volume_MA20"]) / result["Volume_MA20"] * 100)
    return {
        "代码": stock_code,
        "名称": stock_name,
        "总市值": round(total_mv, 2),
        "日期": result["日期"],
        "收盘": result["收盘"],
        "成交额": round(result["成交额"]/10000, 2),  # 成交金额，单位转为万元
        "1日均量": round(result["Volume_MA1"]/10000, 2),
        "20日均量": round(result["Volume_MA20"]/10000, 2),
        "量比": round(volume_increase, 2),
        "涨跌幅": round(result["涨跌幅"], 2),
        "次日涨跌幅": round(result["次日涨跌幅"], 2) if "次日涨跌幅" in result else None,
        "Score": result["Score"]
    }

# 添加配置参数
CONFIG = {
    "start_date": "20250301",
    "end_date": "20250603",
    "target_date": "20250603",
    "single_stock": "",
    "limit_up_threshold": 9.8,
    "request_batch_size": 100,
    "batch_sleep_time": 60,
    "request_timeout": 20,
    "min_market_value": 40e8,
    "max_market_value": 200e8,
    "max_results": 20,
}

# 统一的输出列定义
OUTPUT_COLUMNS = {
    "代码": "代码",
    "名称": "名称",
    "总市值": "总市值(亿)",
    "日期": "日期",
    "收盘": "收盘",
    "成交额": "成交额(万元)",
    "1日均量": "1日均量(万手)",
    "20日均量": "20日均量(万手)",
    "量比": "量比(%)",
    "涨跌幅": "涨跌幅(%)",
    "次日涨跌幅": "次日涨跌幅(%)",
    "Score": "评分"
}

def get_stock_data(symbol, start_date, end_date):
    try:
        return ak.stock_zh_a_hist(
            symbol=symbol,
            period="daily",
            start_date=start_date,
            end_date=end_date,
            adjust="qfq",
            timeout=CONFIG["request_timeout"],  # 使用配置变量
        )
    except Exception as e:
        logging.error(f"获取股票数据失败: {e}")
        return pd.DataFrame()

def filter_stocks():
    stock_info = ak.stock_zh_a_spot_em()
    # 过滤科创板、新股、ST、退市、停牌、总市值范围
    stock_info = stock_info[~stock_info["代码"].str.startswith(("4", "8", "9", "68", "bj"))]
    stock_info = stock_info[~stock_info["代码"].str.startswith(("N", "C"))]
    stock_info = stock_info[~stock_info["名称"].str.contains("ST")]
    stock_info = stock_info[~stock_info["名称"].str.contains("退市")]
    stock_info = stock_info[stock_info["成交量"] != 0]
    # 使用配置变量
    stock_info = stock_info[(stock_info["总市值"] > CONFIG["min_market_value"]) & (stock_info["总市值"] < CONFIG["max_market_value"])]
    stock_info = stock_info.sort_values(by="代码")
    # 将总市值单位转换为亿元
    stock_info["总市值"] = stock_info["总市值"] / 1e8
    return stock_info[["代码", "名称", "总市值"]]

def candidate_stock_strategy(stock_data, target_date):
    """
    @param stock_data: 完整的股票数据
    @param target_date: 要分析的指定日期
    """
    if len(stock_data) < 26:
        logging.warning("数据量不足以计算指标")
        return None

    # 将日期列转换为datetime类型
    stock_data['日期'] = pd.to_datetime(stock_data['日期'])
    target_date = pd.to_datetime(target_date)   
    # 获取指定日期之前(包含)的数据进行分析
    analysis_data = stock_data[stock_data['日期'] <= target_date].copy()
    if len(analysis_data) < 26:
        return None

    # 计算3日、5日均线
    analysis_data["MA3"] = analysis_data["收盘"].rolling(window=3).mean()
    analysis_data["MA5"] = analysis_data["收盘"].rolling(window=5).mean()

    # 因子1：收盘价在3日线和5日线上
    analysis_data["Factor1"] = ((analysis_data["收盘"] > analysis_data["MA3"]) & 
                               (analysis_data["收盘"] > analysis_data["MA5"])).astype(int)
    
    # 因子3：最新1天的成交量超过20日均量的200%
    analysis_data["Volume_MA20"] = analysis_data["成交量"].rolling(window=20).mean()
    analysis_data["Volume_MA1"] = analysis_data["成交量"].rolling(window=1).mean()
    analysis_data["Factor3"] = (analysis_data["成交量"] > analysis_data["Volume_MA20"] * 2).astype(int)
    
    # 删除有问题的代码块 - 我们已经在第50行有了正确的 analysis_data
    # if not needs_modification:
    #     analysis_data = stock_data[stock_data['日期'] <= target_date]
    
    # MACD
    exp12 = analysis_data['收盘'].ewm(span=12, adjust=False).mean()
    exp26 = analysis_data['收盘'].ewm(span=26, adjust=False).mean()
    analysis_data['MACD'] = exp12 - exp26
    analysis_data['Signal'] = analysis_data['MACD'].ewm(span=9, adjust=False).mean()
    analysis_data["Factor4"] = (analysis_data["MACD"] > analysis_data["Signal"]).astype(int)

    # 因子5：当日非涨停
    analysis_data["涨跌幅"] = (analysis_data["收盘"] - analysis_data["收盘"].shift(1)) / analysis_data["收盘"].shift(1) * 100
    analysis_data["Factor5"] = (analysis_data["涨跌幅"] < CONFIG["limit_up_threshold"]).astype(int)  # 使用配置变量
    
    # 因子6：最近未创30日新高
    analysis_data["30日新高"] = analysis_data["收盘"].rolling(window=30).max()
    analysis_data["Factor6"] = (analysis_data["收盘"] <= analysis_data["30日新高"]).astype(int)

    # 因子7：收阳线或平盘
    analysis_data["Factor7"] = (analysis_data["收盘"] >= analysis_data["开盘"]).astype(int)

    # 计算评分
    analysis_data["Score"] = (
        0.20 * analysis_data["Factor1"] +
        0.15 * analysis_data["Factor3"] +
        0.15 * analysis_data["Factor4"] +
        0.15 * analysis_data["Factor5"] +
        0.15 * analysis_data["Factor6"] +
        0.20 * analysis_data["Factor7"]
    )
        
    result = analysis_data.sort_values(by="日期", ascending=False).iloc[0]
    
    # 计算次日涨跌幅（如果有下一个交易日的数据）
    next_day_data = stock_data[stock_data["日期"] > target_date].sort_values("日期")
    if not next_day_data.empty:
        next_day = next_day_data.iloc[0]
        result["次日涨跌幅"] = (next_day["收盘"] - result["收盘"]) / result["收盘"] * 100
    
    return result

# 在文件开头添加函数
def get_stock_name(stock_code):
    """获取股票名称的轻量级函数"""
    try:
        stock_info = ak.stock_info_a_code_name()
        stock_name = stock_info[stock_info['code'] == stock_code]['name'].values[0]
        return stock_name
    except (IndexError, KeyError, AttributeError) as e:
        logging.warning(f"获取股票{stock_code}名称失败: {e}")
        return "未知"
    except Exception as e:
        logging.error(f"获取股票信息时发生未知错误: {e}")
        return "未知"

def get_stock_info(stock_code):
    """获取股票名称和市值信息的统一函数"""
    try:
        spot_info = ak.stock_zh_a_spot_em()
        stock_row = spot_info[spot_info["代码"] == stock_code]
        if not stock_row.empty:
            return {
                "name": stock_row["名称"].values[0],
                "market_value": float(stock_row["总市值"].values[0]) / 1e8
            }
    except:
        pass
    return {"name": "未知", "market_value": 0}

# 示例用法
if __name__ == "__main__":
    # 设置pandas显示选项
    pd.set_option('display.max_columns', None)
    pd.set_option('display.width', None)
    pd.set_option('display.float_format', lambda x: '%.2f' % x)
    pd.set_option('display.unicode.ambiguous_as_wide', True)
    pd.set_option('display.unicode.east_asian_width', True)
    
    target_date = CONFIG["target_date"]
    results = []
    
    if CONFIG["single_stock"]:
        # 单独分析指定股票
        stock_data = get_stock_data(CONFIG["single_stock"], CONFIG["start_date"], CONFIG["end_date"])
        if not stock_data.empty:
            result = candidate_stock_strategy(stock_data, target_date)
            if result is not None:
                try:
                    stock_name = get_stock_name(CONFIG["single_stock"])
                    spot_info = ak.stock_zh_a_spot_em()
                    total_mv = float(spot_info[spot_info["代码"] == CONFIG["single_stock"]]["总市值"].values[0]) / 1e8
                except:
                    stock_name = "未知"
                    total_mv = 0
                
                # 使用统一的格式化函数
                formatted_result = format_result(CONFIG["single_stock"], stock_name, total_mv, result)
                results.append(formatted_result)
                
                df_result = pd.DataFrame(results)
                df_result = df_result.rename(columns=OUTPUT_COLUMNS)
                print(df_result[list(columns.values())])
                print(f"\n分析日期: {target_date}")
                print(f"股票 {CONFIG['single_stock']} 分析完成")
            else:
                print(f"\n股票 {CONFIG['single_stock']} 数据不足以进行分析")
        else:
            print(f"\n获取股票 {CONFIG['single_stock']} 数据失败")
    else:
        # 批量筛选逻辑
        stock_list = filter_stocks()
        total_stocks = len(stock_list)
        print(f"筛选后股票数量: {total_stocks}")
        request_count = 0
        collected_count = 0  # 已收集的满分股票数量
        max_results = 20     # 最大结果数量限制
        
        for idx, row in stock_list.iterrows():
            # 如果已收集足够数量的结果，提前退出
            if collected_count >= max_results:
                print(f"\n已收集到{max_results}条满分股票，停止搜索")
                break
                
            request_count += 1
            
            # 显示进度，包含已收集数据数量
            print(f"\r进度: {request_count}/{total_stocks} ({(request_count/total_stocks*100):.1f}%) - 已收集: {collected_count}条", end="")
            
            # 每批次请求后强制休息 - 使用配置变量
            if request_count % CONFIG["request_batch_size"] == 0:
                print(f"\n已发送{request_count}个请求,暂停{CONFIG['batch_sleep_time']}秒...")
                time.sleep(CONFIG["batch_sleep_time"])
            
            stock_code = row["代码"]
            stock_data = get_stock_data(stock_code, CONFIG["start_date"], CONFIG["end_date"])
            if not stock_data.empty:
                result = candidate_stock_strategy(stock_data, CONFIG["target_date"])  # 使用配置变量
                if result is not None and result["Score"] == 1.0:
                    # 使用统一的格式化函数
                    formatted_result = format_result(stock_code, row["名称"], row["总市值"], result)
                    results.append(formatted_result)
                    collected_count += 1  # 增加已收集数量

        # 批量分析部分（第272行左右）
        df_result = pd.DataFrame(results)
        if not df_result.empty:
            print("\n满分股票列表:")
            df_result = df_result.rename(columns=OUTPUT_COLUMNS)
            df_result = df_result.sort_values(by="量比(%)", ascending=False)
            print(df_result[list(OUTPUT_COLUMNS.values())])
            print(f"\n分析日期: {target_date}")
        else:
            print("\n未找到满分股票")