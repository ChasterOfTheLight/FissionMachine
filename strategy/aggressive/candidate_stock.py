import akshare as ak
import pandas as pd
import logging

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

def candidate_stock_strategy(stock_data):
    if len(stock_data) < 26:
        logging.warning("数据量不足以计算指标")
        return None

    # 计算3日、5日均线
    stock_data["MA3"] = stock_data["收盘"].rolling(window=3).mean()
    stock_data["MA5"] = stock_data["收盘"].rolling(window=5).mean()

    # 因子1：收盘价在3日线和5日线上
    stock_data["Factor1"] = ((stock_data["收盘"] > stock_data["MA3"]) & 
                            (stock_data["收盘"] > stock_data["MA5"])).astype(int)
    
    # 因子3：最新1天的成交量超过20日均量的120%
    stock_data["Volume_MA20"] = stock_data["成交量"].rolling(window=20).mean()
    stock_data["Volume_MA1"] = stock_data["成交量"].rolling(window=1).mean()
    # 修改放量判断标准为超过20日均量的20%
    stock_data["Factor3"] = (stock_data["Volume_MA1"] > stock_data["Volume_MA20"] * 1.2).astype(int)

    # MACD
    exp12 = stock_data['收盘'].ewm(span=12, adjust=False).mean()
    exp26 = stock_data['收盘'].ewm(span=26, adjust=False).mean()
    stock_data['MACD'] = exp12 - exp26
    stock_data['Signal'] = stock_data['MACD'].ewm(span=9, adjust=False).mean()
    # 因子4：MACD向上（MACD大于Signal线）
    stock_data["Factor4"] = (stock_data["MACD"] > stock_data["Signal"]).astype(int)

    # 因子5：当日非涨停（涨幅小于9.8%）
    stock_data["涨跌幅"] = (stock_data["收盘"] - stock_data["收盘"].shift(1)) / stock_data["收盘"].shift(1) * 100
    stock_data["Factor5"] = (stock_data["涨跌幅"] < 9.8).astype(int)
    
    # 因子6：最近未创新高（当前价格低于20日新高）
    stock_data["20日新高"] = stock_data["收盘"].rolling(window=20).max()
    stock_data["Factor6"] = (stock_data["收盘"] < stock_data["20日新高"]).astype(int)

    # 更新评分权重 (重新分配因子2的权重)
    stock_data["Score"] = (
        0.25 * stock_data["Factor1"] +  # 5日线突破
        0.20 * stock_data["Factor3"] +  # 放量确认
        0.20 * stock_data["Factor4"] +  # MACD向上
        0.15 * stock_data["Factor5"] +  # 非涨停
        0.20 * stock_data["Factor6"]    # 未突破新高
    )

    # 返回最新一行
    latest = stock_data.sort_values(by="日期", ascending=False).iloc[0]
    return latest

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
    results = []
    
    for idx, row in stock_list.iterrows():
        if len([r for r in results if r["Score"] == 1.0]) >= 20:
            break
            
        stock_code = row["代码"]
        stock_data = get_stock_data(stock_code, "20250401", "20250527")
        if not stock_data.empty:
            result = candidate_stock_strategy(stock_data)
            if result is not None and result["Score"] == 1.0:
                results.append({
                    "代码": stock_code,
                    "名称": row["名称"],
                    "总市值": round(row["总市值"], 2),
                    "日期": result["日期"],
                    "收盘": result["收盘"],
                    "1日均量": round(result["Volume_MA1"]/10000, 2),  # 1日平均成交量（万手）
                    "20日均量": round(result["Volume_MA20"]/10000, 2),  # 20日平均成交量（万手）
                    "20日新高": round(result["20日新高"]),  # 20日平均成交量（万手）
                    "涨跌幅": round(result["涨跌幅"], 2),
                    "Score": result["Score"]
                })
                print(f"已找到 {len([r for r in results if r['Score'] == 1.0])} 个满分股票")
                
    df_result = pd.DataFrame(results)
    if not df_result.empty:
        print("\n满分股票列表:")
        # 重新排列显示列的顺序并设置列名
        columns = {
            "代码": "代码",
            "名称": "名称",
            "总市值": "总市值(亿)",
            "日期": "日期",
            "收盘": "收盘",
            "1日均量": "1日均量(万手)",
            "20日均量": "20日均量(万手)",
            "涨跌幅": "涨跌幅(%)",
            "Score": "评分"
        }
        df_result = df_result.rename(columns=columns)
        print(df_result[list(columns.values())])
    else:
        print("\n未找到满分股票")