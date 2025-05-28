import akshare as ak
import pandas as pd
import logging
from datetime import datetime

def get_stock_data(symbol, start_date, end_date):
    try:
        return ak.stock_zh_a_hist(
            symbol=symbol,
            period="daily",
            start_date=start_date,
            end_date=end_date,
            adjust="qfq",
            timeout=20,
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
    stock_info = stock_info[(stock_info["总市值"] > 40e8) & (stock_info["总市值"] < 200e8)]
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
    analysis_data["Factor3"] = (analysis_data["Volume_MA1"] > analysis_data["Volume_MA20"] * 2).astype(int)

    # MACD
    exp12 = analysis_data['收盘'].ewm(span=12, adjust=False).mean()
    exp26 = analysis_data['收盘'].ewm(span=26, adjust=False).mean()
    analysis_data['MACD'] = exp12 - exp26
    analysis_data['Signal'] = analysis_data['MACD'].ewm(span=9, adjust=False).mean()
    analysis_data["Factor4"] = (analysis_data["MACD"] > analysis_data["Signal"]).astype(int)

    # 因子5：当日非涨停
    analysis_data["涨跌幅"] = (analysis_data["收盘"] - analysis_data["收盘"].shift(1)) / analysis_data["收盘"].shift(1) * 100
    analysis_data["Factor5"] = (analysis_data["涨跌幅"] < 9.8).astype(int)
    
    # 因子6：最近未创新高
    analysis_data["20日新高"] = analysis_data["收盘"].rolling(window=20).max()
    analysis_data["Factor6"] = (analysis_data["收盘"] < analysis_data["20日新高"]).astype(int)

    # 因子7：非高开低走
    analysis_data["开盘涨幅"] = (analysis_data["开盘"] - analysis_data["收盘"].shift(1)) / analysis_data["收盘"].shift(1) * 100
    analysis_data["Factor7"] = (~((analysis_data["开盘涨幅"] > 0) & (analysis_data["收盘"] >= analysis_data["开盘"]))).astype(int)

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

# 示例用法
if __name__ == "__main__":
    # 设置pandas显示选项
    pd.set_option('display.max_columns', None)  # 显示所有列
    pd.set_option('display.width', None)  # 显示的宽度无限制
    pd.set_option('display.float_format', lambda x: '%.2f' % x)  # 浮点数格式
    pd.set_option('display.unicode.ambiguous_as_wide', True)  # 处理中文对齐
    pd.set_option('display.unicode.east_asian_width', True)  # 处理中文对齐
    
    stock_list = filter_stocks()
    print(f"筛选后股票数量: {len(stock_list)}")
    target_date = "20250528"  # 指定要分析的日期
    results = []
    
    for idx, row in stock_list.iterrows():
        if len([r for r in results if r["Score"] == 1.0]) >= 20:
            print("当前执行到的位置:", idx)
            break
            
        stock_code = row["代码"]
        # 获取更长时间范围的数据
        stock_data = get_stock_data(stock_code, "20250401", "20250528")
        if not stock_data.empty:
            result = candidate_stock_strategy(stock_data, target_date)
            if result is not None and result["Score"] == 1.0:
                # 计算成交量增量百分比
                volume_increase = ((result["Volume_MA1"] - result["Volume_MA20"]) / result["Volume_MA20"] * 100)
                results.append({
                    "代码": stock_code,
                    "名称": row["名称"],
                    "总市值": round(row["总市值"], 2),
                    "日期": result["日期"],
                    "收盘": result["收盘"],
                    "1日均量": round(result["Volume_MA1"]/10000, 2),
                    "20日均量": round(result["Volume_MA20"]/10000, 2),
                    "量比": round(volume_increase, 2),  # 添加成交量增量百分比
                    "涨跌幅": round(result["涨跌幅"], 2),
                    "次日涨跌幅": round(result["次日涨跌幅"], 2) if "次日涨跌幅" in result else None,
                    "Score": result["Score"]
                })

    # 输出结果
    df_result = pd.DataFrame(results)
    if not df_result.empty:
        print("\n满分股票列表:")
        columns = {
            "代码": "代码",
            "名称": "名称",
            "总市值": "总市值(亿)",
            "日期": "日期",
            "收盘": "收盘",
            "1日均量": "1日均量(万手)",
            "20日均量": "20日均量(万手)",
            "量比": "量比(%)",
            "涨跌幅": "涨跌幅(%)",
            "次日涨跌幅": "次日涨跌幅(%)",
            "Score": "评分"
        }
        df_result = df_result.rename(columns=columns)
        # 按量比降序排序
        df_result = df_result.sort_values(by="量比(%)", ascending=False)
        print(df_result[list(columns.values())])
        print(f"\n分析日期: {target_date}")
    else:
        print("\n未找到满分股票")