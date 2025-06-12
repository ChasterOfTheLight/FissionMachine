import akshare as ak
import pandas as pd
import logging
from datetime import datetime
import time
import sys
import traceback

# 配置日志格式
def setup_logger():
    """配置日志格式和输出"""
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s [%(levelname)s] %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S',
        handlers=[
            logging.StreamHandler(sys.stdout),
            logging.FileHandler(f'stock_analysis_{datetime.now().strftime("%Y%m%d")}.log')
        ]
    )

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
        "成交额": round(result["成交额"]/10000, 2),
        "1日均量": round(result["Volume_MA1"]/10000, 2),
        "20日均量": round(result["Volume_MA20"]/10000, 2),
        "量比": round(volume_increase, 2),
        "前20日涨幅": round(result["前20日涨幅"], 2) if "前20日涨幅" in result and pd.notna(result["前20日涨幅"]) else None,
        "涨跌幅": round(result["涨跌幅"], 2),
        "次日涨跌幅": round(result["次日涨跌幅"], 2) if "次日涨跌幅" in result else None,
        "Score": result["Score"]
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
    "前20日涨幅": "前20日涨幅(%)",
    "涨跌幅": "涨跌幅(%)",
    "次日涨跌幅": "次日涨跌幅(%)",
    "Score": "评分"
}

# 添加配置参数
CONFIG = {
    "start_date": "20250301",
    "end_date": "20250612",
    "target_date": "20250612",
    "single_stock": "",
    "limit_up_threshold": 9.8,
    "request_batch_size": 400,
    "batch_sleep_time": 480,
    "request_timeout": 20,
    "min_market_value": 40e8,
    "max_market_value": 200e8,
    "max_results": 20,
}

def get_stock_data(symbol, start_date, end_date):
    """获取股票数据，增加错误详情"""
    try:
        data = ak.stock_zh_a_hist(
            symbol=symbol,
            period="daily",
            start_date=start_date,
            end_date=end_date,
            adjust="qfq",
            timeout=CONFIG["request_timeout"],
        )
        return data
    except Exception as e:
        error_info = traceback.format_exc()
        logging.error(f"获取股票 {symbol} 数据失败:\n错误类型: {type(e).__name__}\n错误信息: {str(e)}\n详细信息:\n{error_info}")
        return pd.DataFrame()

def filter_stocks():
    """过滤股票，增加处理进度日志"""
    try:
        logging.info("开始获取股票列表...")
        start_time = time.time()
        stock_info = ak.stock_zh_a_spot_em()
        logging.info(f"获取原始股票数据完成，共 {len(stock_info)} 只股票")

        # 过滤过程
        initial_count = len(stock_info)
        stock_info = stock_info[~stock_info["代码"].str.startswith(("4", "8", "9", "68", "bj"))]
        logging.info(f"过滤科创板等后剩余: {len(stock_info)} 只")
        
        stock_info = stock_info[~stock_info["代码"].str.startswith(("N", "C"))]
        logging.info(f"过滤新股后剩余: {len(stock_info)} 只")
        
        stock_info = stock_info[~stock_info["名称"].str.contains("ST")]
        stock_info = stock_info[~stock_info["名称"].str.contains("退市")]
        logging.info(f"过滤ST和退市股后剩余: {len(stock_info)} 只")
        
        stock_info = stock_info[stock_info["成交量"] != 0]
        logging.info(f"过滤停牌股后剩余: {len(stock_info)} 只")
        
        # 市值过滤
        stock_info = stock_info[
            (stock_info["总市值"] > CONFIG["min_market_value"]) & 
            (stock_info["总市值"] < CONFIG["max_market_value"])
        ]
        
        stock_info = stock_info.sort_values(by="代码")
        stock_info["总市值"] = stock_info["总市值"] / 1e8
        
        final_count = len(stock_info)
        elapsed_time = time.time() - start_time
        
        logging.info(f"股票筛选完成，耗时: {elapsed_time:.2f}秒")
        logging.info(f"筛选前: {initial_count} 只，筛选后: {final_count} 只")
        logging.info(f"市值范围: {CONFIG['min_market_value']/1e8:.0f}亿 - {CONFIG['max_market_value']/1e8:.0f}亿")
        
        return stock_info[["代码", "名称", "总市值"]]
    except Exception as e:
        error_info = traceback.format_exc()
        logging.error(f"股票筛选过程发生错误:\n错误类型: {type(e).__name__}\n错误信息: {str(e)}\n详细信息:\n{error_info}")
        return pd.DataFrame()

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
    if len(analysis_data) < 30:
        return None

    # 计算3日、5日均线
    analysis_data["MA3"] = analysis_data["收盘"].rolling(window=3).mean()
    analysis_data["MA5"] = analysis_data["收盘"].rolling(window=5).mean()

    # 因子1：收盘价在3日线和5日线上并且为正
    analysis_data["Factor1"] = ((analysis_data["收盘"] > analysis_data["MA3"]) &
                               (analysis_data["收盘"] > analysis_data["MA5"])).astype(int) & (analysis_data["涨跌幅"] > 0).astype(int)
    
    
    # 因子3：最新1天的成交量超过20日均量的200%
    analysis_data["Volume_MA20"] = analysis_data["成交量"].rolling(window=20).mean()
    analysis_data["Volume_MA1"] = analysis_data["成交量"].rolling(window=1).mean()
    analysis_data["Factor3"] = (analysis_data["成交量"] > analysis_data["Volume_MA20"] * 2).astype(int)
    
    # 新增：计算前20交易日涨幅
    analysis_data["20日前收盘"] = analysis_data["收盘"].shift(20)
    analysis_data["前20日涨幅"] = ((analysis_data["收盘"] - analysis_data["20日前收盘"]) / analysis_data["20日前收盘"] * 100)
    
    # MACD
    exp12 = analysis_data['收盘'].ewm(span=12, adjust=False).mean()
    exp26 = analysis_data['收盘'].ewm(span=26, adjust=False).mean()
    analysis_data['MACD'] = exp12 - exp26
    analysis_data['Signal'] = analysis_data['MACD'].ewm(span=9, adjust=False).mean()
    analysis_data["Factor4"] = (analysis_data["MACD"] > analysis_data["Signal"]).astype(int)

    # 因子5：前20日涨幅小于30%
    analysis_data["Factor5"] = (analysis_data["前20日涨幅"] < 30).astype(int)

    # analysis_data["涨跌幅"] = (analysis_data["收盘"] - analysis_data["收盘"].shift(1)) / analysis_data["收盘"].shift(1) * 100
    # analysis_data["Factor5"] = (analysis_data["涨跌幅"] < CONFIG["limit_up_threshold"]).astype(int)  # 使用配置变量
    
    # 因子6：最近未创30日新高
    analysis_data["30日新高"] = analysis_data["收盘"].rolling(window=30).max()
    analysis_data["Factor6"] = (analysis_data["收盘"] <= analysis_data["30日新高"]).astype(int)

    # 因子7：收阳线或平盘
    analysis_data["Factor7"] = (analysis_data["收盘"] >= analysis_data["开盘"]).astype(int)

    # 计算评分
    analysis_data["Score"] = (
        0.20 * analysis_data["Factor1"] +    # 5日线突破
        0.20 * analysis_data["Factor3"] +    # 成交额超20日均量
        0.15 * analysis_data["Factor4"] +    # MACD金叉
        0.10 * analysis_data["Factor5"] +  # 前20日涨幅小于30%
        0.15 * analysis_data["Factor6"] +    # 最近未创30日新高
        0.20 * analysis_data["Factor7"]      # 收阳线或平盘
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
    # 设置日志
    setup_logger()
    logging.info("程序开始运行...")
    logging.info(f"配置信息: {CONFIG}")
    
    # 设置pandas显示选项
    pd.set_option('display.max_columns', None)
    pd.set_option('display.width', None)
    pd.set_option('display.float_format', lambda x: '%.2f' % x)
    pd.set_option('display.unicode.ambiguous_as_wide', True)
    pd.set_option('display.unicode.east_asian_width', True)
    
    target_date = CONFIG["target_date"]
    results = []
    
    if CONFIG["single_stock"]:
        logging.info(f"开始分析单只股票: {CONFIG['single_stock']}")
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
                print(df_result[list(OUTPUT_COLUMNS.values())])
                print(f"\n分析日期: {target_date}")
                print(f"股票 {CONFIG['single_stock']} 分析完成")
            else:
                print(f"\n股票 {CONFIG['single_stock']} 数据不足以进行分析")
        else:
            print(f"\n获取股票 {CONFIG['single_stock']} 数据失败")
    else:
        logging.info("开始批量分析股票...")
        stock_list = filter_stocks()
        if stock_list.empty:
            logging.error("股票筛选失败，程序退出")
            sys.exit(1)
            
        total_stocks = len(stock_list)
        logging.info(f"开始分析 {total_stocks} 只股票")
        start_time = time.time()
        
        request_count = 0
        collected_count = 0  # 已收集的满分股票数量
        max_results = CONFIG["max_results"]     # 最大结果数量限制
        
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
            # 短暂暂停一会
            time.sleep(0.15)

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
        
        elapsed_time = time.time() - start_time
        logging.info(f"分析完成，总耗时: {elapsed_time:.2f}秒")
        logging.info(f"共找到 {len(results)} 只满分股票")